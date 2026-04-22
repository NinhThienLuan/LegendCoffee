package fpt.legendcoffee.service.serviceImpl;

import java.util.List;

import org.springframework.stereotype.Service;

import fpt.legendcoffee.entity.Wallet;
import fpt.legendcoffee.repository.WalletRepository;
import fpt.legendcoffee.service.WalletService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {
    private final WalletRepository walletRepository;

    @Override
    public List<Wallet> getAllWallets() {
        return walletRepository.findAll();
    }
}
