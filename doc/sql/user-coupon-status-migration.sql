-- UserCouponEntity inherits the platform's generic status field. Keep the
-- coupon lifecycle state in its own column so MyBatis does not insert status twice.
ALTER TABLE user_coupon
  ADD COLUMN coupon_status VARCHAR(32) NOT NULL DEFAULT 'UNUSED' COMMENT '券状态 UNUSED/LOCKED/PARTIAL_USED/USED/EXPIRED/INVALID' AFTER coupon_no;

-- Preserve existing coupon lifecycle values that were historically stored in status.
UPDATE user_coupon
SET coupon_status = status
WHERE status IN ('UNUSED', 'LOCKED', 'PARTIAL_USED', 'USED', 'EXPIRED', 'INVALID');

-- The inherited status field is the platform's enabled flag, not the coupon lifecycle state.
UPDATE user_coupon
SET status = 1
WHERE status IN ('UNUSED', 'LOCKED', 'PARTIAL_USED', 'USED', 'EXPIRED', 'INVALID');
