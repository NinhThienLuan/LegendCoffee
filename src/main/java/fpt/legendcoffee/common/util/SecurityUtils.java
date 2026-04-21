package fpt.legendcoffee.common.util;
import fpt.legendcoffee.common.security.CustomUserDetails;
import fpt.legendcoffee.entity.User;

import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@UtilityClass
public class SecurityUtils {

    /**
     * Lấy ID của người dùng hiện tại từ CustomUserDetails
     */
    public static Long getCurrentUserId() {
        User user = getCurrentUser();
        return (user != null) ? user.getId() : null;
    }

    /**
     * Lấy toàn bộ đối tượng User thực thể từ SecurityContext
     */
    public static User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            // Vì CustomUserDetails của bạn có chứa private final User user;
            return userDetails.getUser();
        }
        
        return null; // Hoặc throw exception tùy bạn muốn xử lý lỗi tập trung hay không
    }

    /**
     * Kiểm tra nhanh xem người dùng hiện tại có Role cụ thể không
     * Ví dụ: SecurityUtils.hasRole("ADMIN")
     */
    public static boolean hasRole(String roleName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return false;

        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + roleName));
    }
}