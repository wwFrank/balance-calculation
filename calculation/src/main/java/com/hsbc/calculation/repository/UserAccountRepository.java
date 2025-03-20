package com.hsbc.calculation.repository;

import com.hsbc.calculation.domain.UserAccountDO;
import org.springframework.data.jpa.repository.JpaRepository;
/**
 * 用户账号余额存储
 */
public interface UserAccountRepository extends JpaRepository<UserAccountDO, String> {
}
