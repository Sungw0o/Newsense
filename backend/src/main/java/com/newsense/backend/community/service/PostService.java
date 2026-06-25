package com.newsense.backend.community.service;

import com.newsense.backend.article.domain.ArticleMeta;
import com.newsense.backend.article.repository.ArticleMetaRepository;
import com.newsense.backend.common.exception.CustomException;
import com.newsense.backend.common.exception.ErrorCode;
import com.newsense.backend.community.domain.Post;
import com.newsense.backend.community.domain.PostComment;
import com.newsense.backend.community.domain.PostReaction;
import com.newsense.backend.community.domain.PostReactionType;
import com.newsense.backend.community.domain.PostReport;
import com.newsense.backend.community.domain.PostType;
import com.newsense.backend.community.dto.CommentCreateRequest;
import com.newsense.backend.community.dto.CommentResponse;
import com.newsense.backend.community.dto.PostCreateRequest;
import com.newsense.backend.community.dto.PostReactionResponse;
import com.newsense.backend.community.dto.PostReportRequest;
import com.newsense.backend.community.dto.PostResponse;
import com.newsense.backend.community.dto.PostSort;
import com.newsense.backend.community.repository.PostCommentRepository;
import com.newsense.backend.community.repository.PostReactionRepository;
import com.newsense.backend.community.repository.PostReportRepository;
import com.newsense.backend.community.repository.PostRepository;
import org.springframework.dao.DataIntegrityViolationException;
import com.newsense.backend.user.domain.User;
import com.newsense.backend.user.domain.UserRole;
import com.newsense.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostReactionRepository postReactionRepository;
    private final PostCommentRepository postCommentRepository;
    private final PostReportRepository postReportRepository;
    private final UserRepository userRepository;
    private final ArticleMetaRepository articleMetaRepository;

    @Transactional
    public PostResponse createPost(Long userId, PostCreateRequest request) {
        User user = findUser(userId);
        PostType postType = request.type() == null ? PostType.GENERAL : request.type();
        if (postType == PostType.NOTICE && user.getRole() != UserRole.ADMIN) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
        ArticleMeta article = request.articleMetaId() == null
                ? null
                : articleMetaRepository.findById(request.articleMetaId())
                        .orElseThrow(() -> new CustomException(ErrorCode.ARTICLE_NOT_FOUND));
        Post post = postRepository.save(Post.create(
                request.title(),
                request.content(),
                user,
                article,
                request.scrapSummaryId(),
                postType
        ));
        return toResponse(post);
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getPosts(Pageable pageable, PostSort sort, String keyword, PostType type) {
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort.toSort());
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        boolean hasType = type != null;

        if (hasKeyword || hasType) {
            String pattern = hasKeyword ? "%" + keyword.toLowerCase() + "%" : null;
            return postRepository.findAll(
                    (root, query, cb) -> {
                        var predicate = cb.conjunction();
                        if (hasType) {
                            predicate = cb.and(predicate, cb.equal(root.get("type"), type));
                        }
                        if (hasKeyword) {
                            predicate = cb.and(predicate, cb.or(
                                    cb.like(cb.lower(root.get("title")), pattern),
                                    cb.like(cb.lower(root.get("content")), pattern)
                            ));
                        }
                        return predicate;
                    },
                    pageRequest
            ).map(this::toResponse);
        }
        return postRepository.findAll(pageRequest).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getNotices() {
        PageRequest pageRequest = PageRequest.of(0, 10, PostSort.LATEST.toSort());
        return postRepository.findAll(
                (root, query, cb) -> cb.equal(root.get("type"), PostType.NOTICE),
                pageRequest
        ).map(this::toResponse).getContent();
    }

    @Transactional
    public PostResponse getPost(Long postId) {
        Post post = findPost(postId);
        post.increaseViewCount();
        return toResponse(post);
    }

    @Transactional
    public PostReactionResponse toggleLike(Long postId, Long userId) {
        return toggleReaction(postId, userId, PostReactionType.LIKE);
    }

    @Transactional
    public PostReactionResponse toggleDislike(Long postId, Long userId) {
        return toggleReaction(postId, userId, PostReactionType.DISLIKE);
    }

    @Transactional
    public CommentResponse createComment(Long postId, Long userId, CommentCreateRequest request) {
        Post post = findPost(postId);
        User user = findUser(userId);
        PostComment comment = postCommentRepository.save(PostComment.create(post, user, request.content()));
        return CommentResponse.from(comment);
    }

    @Transactional(readOnly = true)
    public Page<CommentResponse> getComments(Long postId, Pageable pageable) {
        if (!postRepository.existsById(postId)) {
            throw new CustomException(ErrorCode.POST_NOT_FOUND);
        }
        return postCommentRepository.findByPostId(postId, pageable).map(CommentResponse::from);
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        PostComment comment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
        if (!comment.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
        postCommentRepository.delete(comment);
    }

    private PostReactionResponse toggleReaction(Long postId, Long userId, PostReactionType requestedType) {
        Post post = findPost(postId);
        User user = findUser(userId);
        PostReaction reaction = postReactionRepository.findByPostIdAndUserId(postId, userId).orElse(null);

        if (reaction == null) {
            applyCount(post, requestedType);
            postReactionRepository.save(PostReaction.create(user, post, requestedType));
            return createReactionResponse(post, requestedType);
        }
        if (reaction.getType() == requestedType) {
            rollbackCount(post, requestedType);
            postReactionRepository.delete(reaction);
            return createReactionResponse(post, null);
        }

        rollbackCount(post, reaction.getType());
        applyCount(post, requestedType);
        reaction.changeType(requestedType);
        return createReactionResponse(post, requestedType);
    }

    private void applyCount(Post post, PostReactionType type) {
        if (type == PostReactionType.LIKE) {
            post.addLike();
        } else {
            post.addDislike();
        }
    }

    private void rollbackCount(Post post, PostReactionType type) {
        if (type == PostReactionType.LIKE) {
            post.removeLike();
        } else {
            post.removeDislike();
        }
    }

    private PostReactionResponse createReactionResponse(Post post, PostReactionType currentType) {
        return new PostReactionResponse(
                post.getId(),
                post.getLikes(),
                post.getDislikes(),
                currentType == PostReactionType.LIKE,
                currentType == PostReactionType.DISLIKE
        );
    }

    private Post findPost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    }

    @Transactional
    public void reportPost(Long postId, Long userId, PostReportRequest request) {
        Post post = findPost(postId);
        User user = findUser(userId);
        try {
            postReportRepository.save(PostReport.create(post, user, request.reason()));
        } catch (DataIntegrityViolationException e) {
            // already reported by this user — ignore silently
        }
    }

    private PostResponse toResponse(Post post) {
        return PostResponse.from(post, postCommentRepository.countByPostId(post.getId()));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));
    }
}
