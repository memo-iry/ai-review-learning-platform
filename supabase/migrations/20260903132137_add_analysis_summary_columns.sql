ALTER TABLE ai_analyses
    ADD COLUMN understanding_score INTEGER,
    ADD COLUMN understood_summary  TEXT,
    ADD COLUMN weakness_summary    TEXT;

ALTER TABLE ai_analyses
    ADD CONSTRAINT ck_analysis_score
    CHECK (understanding_score IS NULL OR understanding_score BETWEEN 0 AND 100);

COMMENT ON COLUMN ai_analyses.understanding_score IS '이해도 점수 | 회고 분석 결과 0~100';
COMMENT ON COLUMN ai_analyses.understood_summary  IS '이해 요약 | 잘 이해한 부분에 대한 서술';
COMMENT ON COLUMN ai_analyses.weakness_summary    IS '취약 요약 | 보완이 필요한 부분에 대한 서술';
