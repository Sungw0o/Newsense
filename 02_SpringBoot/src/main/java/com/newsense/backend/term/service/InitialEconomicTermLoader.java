package com.newsense.backend.term.service;

import com.newsense.backend.term.domain.Term;
import com.newsense.backend.term.repository.TermRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InitialEconomicTermLoader implements CommandLineRunner {

    private static final String SOURCE = "Newsense 초기 경제 용어 사전";

    private final TermRepository termRepository;

    @Override
    @Transactional
    public void run(String... args) {
        for (SeedTerm term : seedTerms()) {
            termRepository.findByName(term.name())
                    .orElseGet(() -> termRepository.save(Term.create(term.name(), term.definition(), SOURCE)));
        }
    }

    private List<SeedTerm> seedTerms() {
        return List.of(
                new SeedTerm("기준금리", "중앙은행이 금융기관과 거래할 때 기준이 되는 정책 금리입니다."),
                new SeedTerm("물가상승률", "상품과 서비스의 전반적인 가격 수준이 일정 기간 얼마나 올랐는지를 나타냅니다."),
                new SeedTerm("소비자물가지수", "가계가 구입하는 상품과 서비스 가격 변화를 종합한 물가 지표입니다."),
                new SeedTerm("생산자물가지수", "기업 간 거래되는 상품과 서비스 가격 변화를 보여주는 지표입니다."),
                new SeedTerm("국내총생산", "한 나라 안에서 일정 기간 생산된 최종 재화와 서비스의 시장 가치 합계입니다."),
                new SeedTerm("경제성장률", "실질 국내총생산이 전기 또는 전년 대비 얼마나 증가했는지를 나타냅니다."),
                new SeedTerm("환율", "한 나라 통화를 다른 나라 통화로 바꿀 때 적용되는 교환 비율입니다."),
                new SeedTerm("원화강세", "원화 가치가 외국 통화 대비 높아지는 현상입니다."),
                new SeedTerm("원화약세", "원화 가치가 외국 통화 대비 낮아지는 현상입니다."),
                new SeedTerm("경상수지", "상품, 서비스, 소득 거래 등 대외 거래에서 발생한 수입과 지출의 차이입니다."),
                new SeedTerm("무역수지", "상품 수출액에서 상품 수입액을 뺀 값입니다."),
                new SeedTerm("재정수지", "정부의 수입과 지출 차이를 나타내는 지표입니다."),
                new SeedTerm("국가채무", "정부가 갚아야 할 채무의 총액입니다."),
                new SeedTerm("통화량", "경제 안에서 유통되는 돈의 양입니다."),
                new SeedTerm("유동성", "자산을 손실 없이 현금화하기 쉬운 정도 또는 시장에 풀린 자금 여건입니다."),
                new SeedTerm("양적완화", "중앙은행이 국채 등을 매입해 시중 유동성을 늘리는 통화정책입니다."),
                new SeedTerm("긴축정책", "물가 안정 등을 위해 금리 인상이나 지출 축소로 수요를 낮추는 정책입니다."),
                new SeedTerm("금리인상", "중앙은행이나 금융기관이 적용 금리를 올리는 일입니다."),
                new SeedTerm("금리인하", "중앙은행이나 금융기관이 적용 금리를 내리는 일입니다."),
                new SeedTerm("채권", "정부나 기업이 돈을 빌리고 원금과 이자를 갚겠다고 약속한 증권입니다."),
                new SeedTerm("국채", "정부가 재정 자금을 조달하기 위해 발행하는 채권입니다."),
                new SeedTerm("회사채", "기업이 자금을 조달하기 위해 발행하는 채권입니다."),
                new SeedTerm("채권금리", "채권 투자자가 얻는 수익률로, 채권 가격과 반대로 움직이는 경향이 있습니다."),
                new SeedTerm("주가지수", "주식시장 전체나 특정 종목군의 가격 변화를 지수화한 값입니다."),
                new SeedTerm("코스피", "한국거래소 유가증권시장에 상장된 주요 주식의 가격 흐름을 나타내는 지수입니다."),
                new SeedTerm("코스닥", "기술주와 성장기업 중심의 한국 주식시장 지수입니다."),
                new SeedTerm("배당", "기업이 이익의 일부를 주주에게 나누어 주는 것입니다."),
                new SeedTerm("시가총액", "상장 주식 수에 현재 주가를 곱한 기업의 시장 가치입니다."),
                new SeedTerm("PER", "주가를 주당순이익으로 나눈 값으로 기업 이익 대비 주가 수준을 보는 지표입니다."),
                new SeedTerm("PBR", "주가를 주당순자산으로 나눈 값으로 순자산 대비 주가 수준을 보는 지표입니다."),
                new SeedTerm("ROE", "자기자본 대비 순이익 비율로 기업이 자본을 얼마나 효율적으로 쓰는지 보여줍니다."),
                new SeedTerm("영업이익", "기업의 본업에서 매출원가와 판매관리비를 뺀 이익입니다."),
                new SeedTerm("순이익", "영업외 손익과 세금 등을 반영한 최종 이익입니다."),
                new SeedTerm("매출액", "기업이 상품이나 서비스를 판매해 얻은 총수입입니다."),
                new SeedTerm("영업이익률", "매출액 대비 영업이익 비율로 본업 수익성을 나타냅니다."),
                new SeedTerm("부채비율", "자기자본 대비 부채의 비율로 재무 안정성을 보는 지표입니다."),
                new SeedTerm("자본잠식", "누적 손실로 자기자본이 줄어 자본금보다 작아진 상태입니다."),
                new SeedTerm("공모주", "기업이 상장이나 자금 조달을 위해 일반 투자자에게 공개 모집하는 주식입니다."),
                new SeedTerm("IPO", "기업이 처음으로 주식을 공개하고 증권시장에 상장하는 절차입니다."),
                new SeedTerm("상장폐지", "거래소에서 주식 거래 자격을 잃어 상장이 취소되는 일입니다."),
                new SeedTerm("공매도", "주식을 빌려 판 뒤 나중에 사서 갚는 투자 방식입니다."),
                new SeedTerm("레버리지", "차입이나 파생상품 등을 활용해 투자 규모와 손익 변동성을 키우는 방식입니다."),
                new SeedTerm("파생상품", "기초자산 가격 변동에 따라 가치가 결정되는 금융상품입니다."),
                new SeedTerm("선물", "미래 특정 시점에 정해진 가격으로 자산을 사고팔기로 한 계약입니다."),
                new SeedTerm("옵션", "정해진 가격에 자산을 사거나 팔 수 있는 권리를 거래하는 상품입니다."),
                new SeedTerm("ETF", "주가지수나 특정 자산을 추종하며 거래소에서 주식처럼 거래되는 펀드입니다."),
                new SeedTerm("펀드", "여러 투자자의 돈을 모아 전문가가 운용하는 집합투자 상품입니다."),
                new SeedTerm("예금자보호", "금융회사가 파산할 때 일정 한도까지 예금 등을 보호하는 제도입니다."),
                new SeedTerm("가계부채", "가계가 금융기관 등에서 빌린 부채의 총액입니다."),
                new SeedTerm("주택담보대출", "주택을 담보로 제공하고 받는 대출입니다."),
                new SeedTerm("총부채원리금상환비율", "연소득 대비 모든 대출의 원리금 상환액 비율을 뜻합니다."),
                new SeedTerm("담보인정비율", "담보 가치 대비 대출 가능 금액의 비율입니다."),
                new SeedTerm("부동산PF", "부동산 개발사업의 미래 현금흐름을 바탕으로 자금을 조달하는 금융 방식입니다."),
                new SeedTerm("공급망", "원재료 조달부터 생산, 물류, 판매까지 이어지는 전체 흐름입니다."),
                new SeedTerm("반도체", "전기 전도성이 도체와 절연체의 중간인 소재와 이를 이용한 전자 부품입니다."),
                new SeedTerm("수출", "국내에서 생산한 상품이나 서비스를 해외에 판매하는 것입니다."),
                new SeedTerm("수입", "해외에서 생산한 상품이나 서비스를 국내로 들여오는 것입니다."),
                new SeedTerm("관세", "수입품 등에 부과되는 세금입니다."),
                new SeedTerm("보조금", "정부가 특정 산업이나 계층을 지원하기 위해 지급하는 금전적 지원입니다."),
                new SeedTerm("탄소중립", "배출한 온실가스와 흡수 또는 감축한 온실가스가 균형을 이루는 상태입니다.")
        );
    }

    private record SeedTerm(String name, String definition) {
    }
}
