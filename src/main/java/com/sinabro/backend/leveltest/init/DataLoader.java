package com.sinabro.backend.leveltest.init;

import com.sinabro.backend.leveltest.entity.ParentQuestion;
import com.sinabro.backend.leveltest.repository.ParentQuestionRepository;
import com.sinabro.backend.leveltest.entity.LevelTestOption;
import com.sinabro.backend.leveltest.entity.LevelTestQuestion;
import com.sinabro.backend.leveltest.repository.LevelTestQuestionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Transactional
@Component
@RequiredArgsConstructor
@Profile("!prod") // 운영에서는 안 돌게 (선택)
public class DataLoader implements CommandLineRunner {

    private final LevelTestQuestionRepository questionRepository;
    private final ParentQuestionRepository parentQuestionRepository;

    // 앱 라이프사이클 동안 1회만 실행하도록 가드
    private static final AtomicBoolean RUN_ONCE = new AtomicBoolean(false);

    @Override
    public void run(String... args) {
        // 이미 실행됐다면 바로 종료
        if (!RUN_ONCE.compareAndSet(false, true)) return;

        // 이미 데이터가 있으면 시딩 스킵 (중복 방지)
        if (parentQuestionRepository.count() > 0 || questionRepository.count() > 0) return;

        // --------------------
        // ↓↓↓ 이하 기존 시딩 로직 그대로 ↓↓↓
        // ✅ 부모 체크 문제들
        ParentQuestion pq1 = new ParentQuestion();
        pq1.setQuestionOrder(1);
        pq1.setQuestionText("부모님이 풉니다. 문항을 읽고 체크해 주세요.");
        pq1.addOption("네. 부모님이 체크하겠습니다.");

        ParentQuestion pq2 = new ParentQuestion();
        pq2.setQuestionOrder(2);
        pq2.setQuestionText("평소 자녀와 주로 어떤 언어로 대화하시나요?");
        pq2.addOption("① 대부분 한국어");
        pq2.addOption("② 한국어 외 다른 언어");

        ParentQuestion pq3 = new ParentQuestion();
        pq3.setQuestionOrder(3);
        pq3.setQuestionText("아이가 간판이나 책의 글자를 보며 “이게 뭐야?” 하고 자주 묻는다");
        pq3.addOption("① O");
        pq3.addOption("② X");

        ParentQuestion pq4 = new ParentQuestion();
        pq4.setQuestionOrder(4);
        pq4.setQuestionText("“과자”, “사과” 같은 쉬운 단어를 혼자 읽을 수 있다");
        pq4.addOption("① O");
        pq4.addOption("② X");
        pq4.addOption("③ 잘 모르겠다");

        ParentQuestion pq5 = new ParentQuestion();
        pq5.setQuestionOrder(5);
        pq5.setQuestionText("“꽃”, “집”, “밥” 같은 받침 있는 단어를 읽을 수 있다");
        pq5.addOption("① O");
        pq5.addOption("② X");
        pq5.addOption("③ 잘 모르겠다");

        ParentQuestion pq6 = new ParentQuestion();
        pq6.setQuestionOrder(6);
        pq6.setQuestionText("아이가 본인의 이름을 정확하게 쓸 줄 안다");
        pq6.addOption("① O");
        pq6.addOption("② X");
        pq6.addOption("③ 잘 모르겠다");

        parentQuestionRepository.saveAll(List.of(pq1, pq2, pq3, pq4, pq5, pq6));

        // ✅ L1 문제들 …
        LevelTestQuestion l1q1 = new LevelTestQuestion();
        l1q1.setLevel(1);
        l1q1.setType("듣고 고르기");
        l1q1.setPrompt("아래 그림 중에서 “사과”를 골라보세요.");
        l1q1.setAudioUrl("/audio/apple.mp3");
        l1q1.addOption(new LevelTestOption("사과", "/img/apple.png", true));
        l1q1.addOption(new LevelTestOption("바나나", "/img/banana.png", false));
        l1q1.addOption(new LevelTestOption("포도", "/img/grape.png", false));

        LevelTestQuestion l1q2 = new LevelTestQuestion();
        l1q2.setLevel(1);
        l1q2.setType("이야기 듣고 고르기");
        l1q2.setPrompt("선생님이 들려주는 이야기를 듣고 이야기의 내용과 같은 그림을 찾아보세요.");
        l1q2.setAudioUrl("/audio/bird_story.mp3");
        l1q2.addOption(new LevelTestOption("나비", "/img/butterfly.png", false));
        l1q2.addOption(new LevelTestOption("새", "/img/bird.png", true));
        l1q2.addOption(new LevelTestOption("구름", "/img/flower.png", false));
        l1q2.addOption(new LevelTestOption("개", "/img/flower.png", false));

        LevelTestQuestion l1q3 = new LevelTestQuestion();
        l1q3.setLevel(1);
        l1q3.setType("호칭 고르기");
        l1q3.setPrompt("음성을 듣고 알맞은 그림을 골라보세요.");
        l1q3.setAudioUrl("/audio/mother.mp3");
        l1q3.addOption(new LevelTestOption("엄마", "/img/mom.png", true));
        l1q3.addOption(new LevelTestOption("아빠", "/img/dad.png", false));
        l1q3.addOption(new LevelTestOption("할머니", "/img/grandmom.png", false));

        questionRepository.saveAll(List.of(l1q1, l1q2, l1q3));

        // ✅ L2
        LevelTestQuestion l2q1 = new LevelTestQuestion();
        l2q1.setLevel(2);
        l2q1.setType("글자 고르기");
        l2q1.setPrompt("아래 보기 중 같은 글자를 골라보세요.");
        l2q1.setQuestionImageUrl("/img/ga.png");
        l2q1.addOption(new LevelTestOption("나", "/img/ma.png", false));
        l2q1.addOption(new LevelTestOption("가", "/img/ga.png", true));
        l2q1.addOption(new LevelTestOption("다", "/img/ra.png", false));

        LevelTestQuestion l2q2 = new LevelTestQuestion();
        l2q2.setLevel(2);
        l2q2.setType("이름 고르기");
        l2q2.setPrompt("아래 보기에서 본인 이름을 골라보세요");
        l2q2.setAudioUrl("/audio/find-your-name.mp3");
        l2q2.addOption(new LevelTestOption("정답_이름_자리", null, true));
        l2q2.addOption(new LevelTestOption("김철수", null, false));
        l2q2.addOption(new LevelTestOption("박영희", null, false));
        l2q2.addOption(new LevelTestOption("홍길동", null, false));

        questionRepository.saveAll(List.of(l2q1, l2q2));

        // ✅ L3
        LevelTestQuestion l3q1 = new LevelTestQuestion();
        l3q1.setLevel(3);
        l3q1.setType("끝말 잇기");
        l3q1.setPrompt("선생님이 들려주는 낱말을 듣고, 끝말의 글자로 시작하는 그림을 골라보세요. 과자 → 자전거 → ?");
        l3q1.setAudioUrl("/audio/word_chain.mp3");
        l3q1.addOption(new LevelTestOption("가위", "/img/scissors.png", false));
        l3q1.addOption(new LevelTestOption("모자", "/img/hat.png", false));
        l3q1.addOption(new LevelTestOption("거미", "/img/spider.png", true));

        LevelTestQuestion l3q2 = new LevelTestQuestion();
        l3q2.setLevel(3);
        l3q2.setType("빈칸 글자 고르기");
        l3q2.setPrompt("그림을 보고, 빠진 글자를 찾아 고르세요.");
        l3q2.setQuestionImageUrl("/img/shoes_blank.png");
        l3q2.addOption(new LevelTestOption("산", "/img/san.png", false));
        l3q2.addOption(new LevelTestOption("심", "/img/so.png", false));
        l3q2.addOption(new LevelTestOption("신", "/img/shin.png", true));

        questionRepository.saveAll(List.of(l3q1, l3q2));
    }
}
