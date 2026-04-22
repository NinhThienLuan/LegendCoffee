package fpt.legendcoffee.entity.enumeration;

import lombok.Getter;

@Getter
public enum BankCode {
    NCB("Ngân hàng NCB"),
    VCB("Vietcombank"),
    VIETINBANK("VietinBank"),
    BIDV("BIDV"),
    AGRIBANK("Agribank"),
    TPBANK("TPBank"),
    MBBANK("MB Bank"),
    TECHCOMBANK("Techcombank"),
    ACB("ACB"),
    VPBANK("VPBank");

    private final String displayName;

    BankCode(String displayName) {
        this.displayName = displayName;
    }
}
