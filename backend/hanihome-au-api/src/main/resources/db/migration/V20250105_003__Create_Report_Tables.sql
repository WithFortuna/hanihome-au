-- Create reports table
CREATE TABLE reports (
    id BIGSERIAL PRIMARY KEY,
    reporter_id BIGINT NOT NULL,
    report_type VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    target_type VARCHAR(50) NOT NULL,
    target_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    assigned_admin_id BIGINT,
    admin_notes TEXT,
    resolution TEXT,
    resolved_at TIMESTAMP,
    assigned_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    priority INTEGER DEFAULT 1,
    auto_moderated BOOLEAN DEFAULT FALSE,
    evidence_urls TEXT
);

-- Create report_actions table
CREATE TABLE report_actions (
    id BIGSERIAL PRIMARY KEY,
    report_id BIGINT NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    performed_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    additional_details TEXT
);

-- Add foreign key constraints
ALTER TABLE report_actions 
ADD CONSTRAINT fk_report_actions_report_id 
FOREIGN KEY (report_id) REFERENCES reports(id) ON DELETE CASCADE;

-- Create indexes for reports table
CREATE INDEX idx_report_status ON reports(status);
CREATE INDEX idx_report_type ON reports(report_type);
CREATE INDEX idx_report_reporter ON reports(reporter_id);
CREATE INDEX idx_report_target ON reports(target_type, target_id);
CREATE INDEX idx_report_created_at ON reports(created_at);
CREATE INDEX idx_report_assigned_admin ON reports(assigned_admin_id);
CREATE INDEX idx_report_priority_status ON reports(priority DESC, status);
CREATE INDEX idx_report_auto_moderated ON reports(auto_moderated, status);

-- Create indexes for report_actions table
CREATE INDEX idx_report_action_report ON report_actions(report_id);
CREATE INDEX idx_report_action_performed_by ON report_actions(performed_by);
CREATE INDEX idx_report_action_created_at ON report_actions(created_at);
CREATE INDEX idx_report_action_type ON report_actions(action_type);

-- Add comments
COMMENT ON TABLE reports IS '신고 테이블 - 사용자가 제출한 모든 신고를 저장';
COMMENT ON COLUMN reports.reporter_id IS '신고자 사용자 ID';
COMMENT ON COLUMN reports.report_type IS '신고 유형 (SPAM_USER, FAKE_LISTING, INAPPROPRIATE_CONTENT 등)';
COMMENT ON COLUMN reports.description IS '신고 내용 설명';
COMMENT ON COLUMN reports.target_type IS '신고 대상 타입 (USER, PROPERTY, TRANSACTION, REVIEW 등)';
COMMENT ON COLUMN reports.target_id IS '신고 대상 ID';
COMMENT ON COLUMN reports.status IS '신고 처리 상태';
COMMENT ON COLUMN reports.assigned_admin_id IS '담당 관리자 ID';
COMMENT ON COLUMN reports.admin_notes IS '관리자 메모';
COMMENT ON COLUMN reports.resolution IS '처리 결과';
COMMENT ON COLUMN reports.priority IS '우선순위 (1: Low, 2: Medium, 3: High, 4: Critical)';
COMMENT ON COLUMN reports.auto_moderated IS '자동 조치 여부';
COMMENT ON COLUMN reports.evidence_urls IS '증거 자료 URL (JSON 배열)';

COMMENT ON TABLE report_actions IS '신고 처리 이력 테이블 - 신고에 대한 모든 조치를 기록';
COMMENT ON COLUMN report_actions.report_id IS '신고 ID';
COMMENT ON COLUMN report_actions.action_type IS '조치 유형 (STATUS_CHANGE, NOTES_ADDED, RESOLVED 등)';
COMMENT ON COLUMN report_actions.description IS '조치 설명';
COMMENT ON COLUMN report_actions.performed_by IS '조치 수행자 ID (NULL이면 시스템 자동 조치)';
COMMENT ON COLUMN report_actions.additional_details IS '추가 상세 정보 (JSON 형태)';

-- Insert some sample report types for reference (optional)
INSERT INTO reports (reporter_id, report_type, description, target_type, target_id, priority) VALUES
(1, 'SPAM_USER', 'This user is sending spam messages', 'USER', 2, 2),
(3, 'FAKE_LISTING', 'This property listing appears to be fake', 'PROPERTY', 1, 3),
(2, 'INAPPROPRIATE_CONTENT', 'Inappropriate content in property description', 'PROPERTY', 2, 1);

-- Update timestamp trigger function (if not exists)
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create trigger for reports table
CREATE TRIGGER update_reports_updated_at 
    BEFORE UPDATE ON reports 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();