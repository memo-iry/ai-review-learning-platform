-- 개념별 이해도에 자연어 서술을 함께 보관한다.
-- 나중에 복습자료 생성 프롬프트에 학습자의 현재 상태를 실어 보내기 위한 것이다.
--
-- score 는 그대로 둔다. 정렬 · 평균 · Level 판정 · 운영자 집계가 숫자에 기대고 있어
-- 텍스트로 대체할 수 없다. 서술은 더하는 것이지 바꾸는 것이 아니다.

ALTER TABLE concept_mastery ADD COLUMN summary TEXT;

COMMENT ON COLUMN concept_mastery.summary IS
    '분석기가 만든 개념별 상태 서술. 파생 값이며 원본이 아니다. '
    '갱신될 때마다 덮어쓰이므로 이력이 필요하면 ai_analyses 와 reflections 를 본다. '
    'AiAnalysisPort 구현체만 이 값을 쓴다. Quiz 채점은 점수만 바꾸고 서술은 건드리지 않는다.';
