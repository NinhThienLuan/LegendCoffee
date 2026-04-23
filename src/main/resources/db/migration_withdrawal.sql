-- =============================================================================
-- MIGRATION: Withdrawal Flow + Reserved Pattern
-- Chạy script này trên database hiện có (MSSQL Server)
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. Cập nhật bảng wallets
--    Đổi tên cột `amount` → `available_amount` và thêm `reserved_amount`
-- -----------------------------------------------------------------------------

-- Bước 1a: Đổi tên cột cũ (MSSQL dùng sp_rename)
EXEC sp_rename 'wallets.amount', 'available_amount', 'COLUMN';

-- Bước 1b: Thêm cột reserved_amount với giá trị mặc định 0
ALTER TABLE wallets
    ADD reserved_amount DECIMAL(18, 2) NOT NULL DEFAULT 0;

-- Bước 1c: Xác nhận dữ liệu cũ vẫn đúng (available_amount giữ nguyên giá trị cũ)
-- SELECT id, available_amount, reserved_amount FROM wallets;

-- -----------------------------------------------------------------------------
-- 2. Tạo bảng withdrawal_requests
-- -----------------------------------------------------------------------------

CREATE TABLE withdrawal_requests (
    id                BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id           BIGINT          NOT NULL,
    amount            DECIMAL(18, 2)  NOT NULL,
    bank_name         NVARCHAR(100)   NOT NULL,
    bank_code         NVARCHAR(20)    NOT NULL,
    account_number    NVARCHAR(50)    NOT NULL,
    account_holder    NVARCHAR(100)   NOT NULL,
    status            NVARCHAR(20)    NOT NULL DEFAULT 'PENDING',  -- PENDING | APPROVED | REJECTED
    reject_reason     NVARCHAR(500)   NULL,
    processed_at      DATETIME        NULL,
    created_at        DATETIME        NOT NULL DEFAULT GETDATE(),
    updated_at        DATETIME        NOT NULL DEFAULT GETDATE(),

    CONSTRAINT FK_withdrawal_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT CHK_withdrawal_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    CONSTRAINT CHK_withdrawal_amount CHECK (amount > 0)
);

-- Index để query nhanh theo user + status (dùng trong existsByUserIdAndStatus)
CREATE INDEX IDX_withdrawal_user_status ON withdrawal_requests(user_id, status);

-- =============================================================================
-- ROLLBACK (nếu cần hoàn tác):
-- DROP TABLE withdrawal_requests;
-- EXEC sp_rename 'wallets.available_amount', 'amount', 'COLUMN';
-- ALTER TABLE wallets DROP COLUMN reserved_amount;
-- =============================================================================
