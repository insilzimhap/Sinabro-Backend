package com.sinabro.backend.leveltest.init;

import com.sinabro.backend.leveltest.entity.ParentQuestion;
import com.sinabro.backend.leveltest.repository.ParentQuestionRepository;
import com.sinabro.backend.leveltest.entity.LevelTestOption;
import com.sinabro.backend.leveltest.entity.LevelTestQuestion;
import com.sinabro.backend.leveltest.repository.LevelTestQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final LevelTestQuestionRepository questionRepository;
    private final ParentQuestionRepository parentQuestionRepository;

    @Override
    public void run(String... args) throws Exception {

        // ✅ 부모 체크 문제들
        ParentQuestion pq1 = new ParentQuestion();
        pq1.setQuestionOrder(1);
        pq1.setQuestionText("부모님이 풉니다. 문항을 읽고 체크해 주세요.");
        pq1.setChoices(List.of("네. 부모님이 체크하겠습니다."));

        ParentQuestion pq2 = new ParentQuestion();
        pq2.setQuestionOrder(2);
        pq2.setQuestionText("평소 자녀와 주로 어떤 언어로 대화하시나요?");
        pq2.setChoices(List.of("① 대부분 한국어", "② 한국어 외 다른 언어"));

        ParentQuestion pq3 = new ParentQuestion();
        pq3.setQuestionOrder(3);
        pq3.setQuestionText("아이가 간판이나 책의 글자를 보며 “이게 뭐야?” 하고 자주 묻는다");
        pq3.setChoices(List.of("① O", "② X"));

        ParentQuestion pq4 = new ParentQuestion();
        pq4.setQuestionOrder(4);
        pq4.setQuestionText("“과자”, “사과” 같은 쉬운 단어를 혼자 읽을 수 있다");
        pq4.setChoices(List.of("① O", "② X", "③ 잘 모르겠다"));

        ParentQuestion pq5 = new ParentQuestion();
        pq5.setQuestionOrder(5);
        pq5.setQuestionText("“꽃”, “집”, “밥” 같은 받침 있는 단어를 읽을 수 있다");
        pq5.setChoices(List.of("① O", "② X", "③ 잘 모르겠다"));

        ParentQuestion pq6 = new ParentQuestion();
        pq6.setQuestionOrder(6);
        pq6.setQuestionText("아이가 본인의 이름을 정확하게 쓸 줄 안다");
        pq6.setChoices(List.of("① O", "② X", "③ 잘 모르겠다"));

        parentQuestionRepository.saveAll(List.of(pq1, pq2, pq3, pq4, pq5, pq6));

        // ✅ L1 문제 1: 사과 고르기
        LevelTestQuestion l1q1 = new LevelTestQuestion();
        l1q1.setLevel(1);
        l1q1.setType("듣고 고르기");
        l1q1.setPrompt("아래 그림 중에서 “사과”를 골라보세요.");
        l1q1.setAudioUrl("/audio/apple.mp3");

        l1q1.addOption(new LevelTestOption("사과", "/img/apple.png", true));
        l1q1.addOption(new LevelTestOption("바나나", "/img/banana.png", false));
        l1q1.addOption(new LevelTestOption("포도", "/img/grape.png", false));

        // ✅ L1 문제 2: 이야기 듣고 고르기
        LevelTestQuestion l1q2 = new LevelTestQuestion();
        l1q2.setLevel(1);
        l1q2.setType("이야기 듣고 고르기");
        l1q2.setPrompt("선생님이 들려주는 이야기를 듣고 이야기의 내용과 같은 그림을 찾아보세요.");
        l1q2.setAudioUrl("/audio/bird_story.mp3");// "새가 훨훨 날고 있어요"

        l1q2.addOption(new LevelTestOption("나비", "/img/butterfly.png", false));
        l1q2.addOption(new LevelTestOption("새", "/img/bird.png", true));
        l1q2.addOption(new LevelTestOption("구름", "/img/cloud.png", false));
        l1q2.addOption(new LevelTestOption("개", "/img/dog.png", false));
        // 보기 사진을 뭐로 할지... 잠자리는 너무 어려울거같기도..

        // ✅ L1 문제 3: 호칭 고르기
        LevelTestQuestion l1q3 = new LevelTestQuestion();
        l1q3.setLevel(1);
        l1q3.setType("호칭 고르기");
        l1q3.setPrompt("음성을 듣고 알맞은 그림을 골라보세요.");
        l1q3.setAudioUrl("/audio/mother.mp3"); //"엄마" 음성

        l1q3.addOption(new LevelTestOption("엄마", "/img/mother.png", true));
        l1q3.addOption(new LevelTestOption("아빠", "/img/father.png", false));
        l1q3.addOption(new LevelTestOption("아기", "/img/baby.png", false));

        questionRepository.save(l1q1);
        questionRepository.save(l1q2);
        questionRepository.save(l1q3);

        // ✅ L2 문제 1: 글자 고르기
        LevelTestQuestion l2q1 = new LevelTestQuestion();
        l2q1.setLevel(2);
        l2q1.setType("글자 고르기");
        l2q1.setPrompt("아래 보기 중 같은 글자를 골라보세요.");
        l2q1.setAudioUrl(null);  // 음성 없음
        l2q1.setQuestionImageUrl("/img/question_ga.png");  // 중앙에 크게 보여줄 기준 글자 이미지

        l2q1.addOption(new LevelTestOption("나", "/img/na.png", false));
        l2q1.addOption(new LevelTestOption("가", "/img/ga.png", true));
        l2q1.addOption(new LevelTestOption("다", "/img/da.png", false));

        // ✅ L2 문제 2: 이름 고르기
        LevelTestQuestion l2q2 = new LevelTestQuestion();
        l2q2.setLevel(2);
        l2q2.setType("이름 고르기");
        l2q2.setPrompt("아래 보기에서 본인 이름을 골라보세요");
        l2q2.setAudioUrl("/audio/find-your-name.mp3"); //"네 이름을 찾아보자~"
        l2q2.setQuestionImageUrl(null);

        questionRepository.save(l2q1);
        questionRepository.save(l2q2);

        // ✅ L3 문제 1: 끝말 잇기
        LevelTestQuestion l3q1 = new LevelTestQuestion();
        l3q1.setLevel(3);
        l3q1.setType("끝말 잇기");
        l3q1.setPrompt("선생님이 들려주는 낱말을 듣고, 끝말의 글자로 시작하는 그림을 골라보세요. 과자 → 자전거 → ?");
        l3q1.setAudioUrl("/audio/word_chain.mp3");  // "과자-> 자전거"
        l3q1.setQuestionImageUrl(null);  // 문제 이미지 없음

        l3q1.addOption(new LevelTestOption("가위", "/img/scissors.png", false));
        l3q1.addOption(new LevelTestOption("모자", "/img/hat.png", false));
        l3q1.addOption(new LevelTestOption("거미", "/img/spider.png", true));

        // ✅ L3 문제 2: 빈칸 글자 고르기
        LevelTestQuestion l3q2 = new LevelTestQuestion();
        l3q2.setLevel(3);
        l3q2.setType("빈칸 글자 고르기");
        l3q2.setPrompt("그림을 보고, 빠진 글자를 찾아 고르세요.");
        l3q2.setAudioUrl(null);
        l3q2.setQuestionImageUrl("/img/shoes_blank.png");  // 문제 이미지 없음

        l3q2.addOption(new LevelTestOption("산", "/img/san.png", false));
        l3q2.addOption(new LevelTestOption("심", "/img/shim.png", false));
        l3q2.addOption(new LevelTestOption("신", "/img/shin.png", true));

        questionRepository.save(l3q1);
        questionRepository.save(l3q2);
    }
}
