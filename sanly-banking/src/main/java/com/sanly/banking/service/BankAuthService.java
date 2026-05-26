package com.sanly.banking.service;

import com.sanly.banking.entity.RegisteredBank;
import com.sanly.banking.exception.BankNotFoundException;
import com.sanly.banking.exception.BankSuspendedException;
import com.sanly.banking.exception.InvalidApiKeyException;
import com.sanly.banking.repository.RegisteredBankRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankAuthService {

    private final RegisteredBankRepository bankRepository;
    private final PasswordEncoder          passwordEncoder;

    /**
     * Validates bank code + API key pair.
     *
     * @return the validated bank if credentials match and bank is ACTIVE
     * @throws BankNotFoundException   if no bank with given code
     * @throws BankSuspendedException  if bank is not ACTIVE
     * @throws InvalidApiKeyException  if API key does not match stored hash
     */
    public RegisteredBank validateBank(String bankCode, String apiKey) {
        RegisteredBank bank = bankRepository.findByBankCode(bankCode)
                .orElseThrow(() -> new BankNotFoundException("Bank not found: " + bankCode));

        if (!"ACTIVE".equals(bank.getStatus())) {
            throw new BankSuspendedException("Bank '" + bankCode + "' is not active (status: " + bank.getStatus() + ")");
        }

        if (!passwordEncoder.matches(apiKey, bank.getApiKeyHash())) {
            log.warn("Invalid API key attempt for bank: {}", bankCode);
            throw new InvalidApiKeyException("Invalid API key for bank: " + bankCode);
        }

        return bank;
    }
}
