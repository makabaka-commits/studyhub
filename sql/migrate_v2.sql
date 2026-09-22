USE studyhub;

ALTER TABLE user
    ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER' AFTER email;

CREATE INDEX idx_user_status ON user (status);
CREATE INDEX idx_note_status_created ON note (status, created_at);
CREATE INDEX idx_note_user ON note (user_id);
CREATE INDEX idx_comment_note_status ON comment (note_id, status);
CREATE INDEX idx_comment_user ON comment (user_id);
CREATE INDEX idx_browse_updated ON browse_history (user_id, updated_at);
CREATE INDEX idx_notification_receiver_read
    ON notification (receiver_id, read_status, created_at);
