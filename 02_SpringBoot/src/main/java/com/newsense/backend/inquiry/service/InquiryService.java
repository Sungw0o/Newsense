package com.newsense.backend.inquiry.service;

import com.newsense.backend.common.exception.CustomException;
import com.newsense.backend.common.exception.ErrorCode;
import com.newsense.backend.inquiry.domain.Inquiry;
import com.newsense.backend.inquiry.dto.InquiryRequest;
import com.newsense.backend.inquiry.dto.InquiryResponse;
import com.newsense.backend.inquiry.repository.InquiryRepository;
import com.newsense.backend.user.domain.User;
import com.newsense.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final UserRepository userRepository;

    @Transactional
    public InquiryResponse submit(Long userId, InquiryRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));
        Inquiry inquiry = Inquiry.of(user, request.title(), request.content());
        return InquiryResponse.from(inquiryRepository.save(inquiry));
    }

    @Transactional(readOnly = true)
    public Page<InquiryResponse> getMyInquiries(Long userId, Pageable pageable) {
        return inquiryRepository.findByUserId(userId, pageable).map(InquiryResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<InquiryResponse> getAllInquiries(Pageable pageable) {
        return inquiryRepository.findAllWithUser(pageable).map(InquiryResponse::from);
    }

    @Transactional
    public InquiryResponse resolve(Long inquiryId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));
        inquiry.resolve();
        return InquiryResponse.from(inquiry);
    }
}
