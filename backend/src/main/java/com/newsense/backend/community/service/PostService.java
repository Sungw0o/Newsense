package com.newsense.backend.community.service;

import com.newsense.backend.article.domain.ArticleMeta;
import com.newsense.backend.article.repository.ArticleMetaRepository;
import com.newsense.backend.common.exception.CustomException;
import com.newsense.backend.common.exception.ErrorCode;
import com.newsense.backend.community.domain.Post;
import com.newsense.backend.community.domain.PostComment;
import com.newsense.backend.community.domain.PostReaction;
import com.newsense.backend.community.domain.PostReactionType;
import com.newsense.backend.community.dto.CommentCreateRequest;
import com.newsense.backend.community.dto.CommentResponse;
import com.newsense.backend.community.dto.PostCreateRequest;
import com.newsense.backend.community.dto.PostReactionResponse;
import com.newsense.backend.community.dto.PostResponse;
import com.newsense.backend.community.dto.PostSort;
import com.newsense.backend.community.repository.PostCommentRepository;
import com.newsense.backend.community.repository.PostReactionRepository;
import com.newsense.backend.community.repository.PostRepository;
import com.newsense.backend.user.domain.User;
import com.newsense.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostReactionRepository postReactionRepository;
    private final PostCommentRepository postCommentRepository;
    private final UserRepository userRepository;
    private final ArticleMetaRepository articleMetaRepository;

    @Transactional
    public PostResponse createPost(Long userId, PostCreateRequest request) {
        User user = findUser(userId);
        ArticleMeta article = request.articleMetaId() == null
                ? null
                : articleMetaRepository.findById(request.articleMetaId())
                        .orElseThrow(() -> new CustomException(ErrorCode.ARTICLE_NOT_FOUND));
        Post post = postRepository.save(Post.create(
                request.title(),
                request.content(),
                user,
                article,
                request.scrapSummaryId()
        ));
        return toResponse(post);
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getPosts(Pageable pageable, PostSort sort) {
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort.toSort());
        return postRepository.findAll(pageRequest).map(this::toResponse);
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

    private PostResponse toResponse(Post post) {
        return PostResponse.from(post, postCommentRepository.countByPostId(post.getId()));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));
    }
}
