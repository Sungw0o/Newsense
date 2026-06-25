package com.newsense.backend.term;

import com.newsense.backend.term.domain.Term;
import com.newsense.backend.term.repository.TermRepository;
import com.newsense.backend.term.service.InitialEconomicTermLoader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("InitialEconomicTermLoader unit tests")
class InitialEconomicTermLoaderTest {

    @InjectMocks
    InitialEconomicTermLoader loader;

    @Mock
    TermRepository termRepository;

    @Test
    void run_allTermsExist_saveNeverCalled() throws Exception {
        Term existing = Term.create("기준금리", "정책 금리", "Newsense 초기 경제 용어 사전");
        given(termRepository.findByName(anyString())).willReturn(Optional.of(existing));

        loader.run();

        verify(termRepository, never()).save(any());
    }

    @Test
    void run_noTermsExist_savesAllSeedTerms() throws Exception {
        given(termRepository.findByName(anyString())).willReturn(Optional.empty());
        given(termRepository.save(any(Term.class))).willAnswer(inv -> inv.getArgument(0));

        loader.run();

        verify(termRepository, atLeastOnce()).save(any(Term.class));
    }
}
