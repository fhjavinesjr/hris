package com.payroll.impl;

import com.payroll.dtos.PayrollPeriodLockDTO;
import com.payroll.entitymodels.PayrollPeriodLock;
import com.payroll.repositories.PayrollPeriodLockRepository;
import com.payroll.services.PayrollPeriodLockService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class PayrollPeriodLockServiceImpl implements PayrollPeriodLockService {

    private final PayrollPeriodLockRepository lockRepo;

    public PayrollPeriodLockServiceImpl(PayrollPeriodLockRepository lockRepo) {
        this.lockRepo = lockRepo;
    }

    @Override
    public PayrollPeriodLockDTO getLockStatus(String salaryPeriodKey) {
        Optional<PayrollPeriodLock> lock = lockRepo.findById(salaryPeriodKey);
        if (lock.isPresent()) {
            PayrollPeriodLock l = lock.get();
            return new PayrollPeriodLockDTO(l.getSalaryPeriodKey(), true, l.getLockedBy(), l.getLockedAt());
        }
        return new PayrollPeriodLockDTO(salaryPeriodKey, false, null, null);
    }

    @Override
    @Transactional
    public PayrollPeriodLockDTO lockPeriod(String salaryPeriodKey, String lockedBy) {
        // Idempotent: return existing lock if already locked
        Optional<PayrollPeriodLock> existing = lockRepo.findById(salaryPeriodKey);
        if (existing.isPresent()) {
            PayrollPeriodLock l = existing.get();
            return new PayrollPeriodLockDTO(l.getSalaryPeriodKey(), true, l.getLockedBy(), l.getLockedAt());
        }

        String by = (lockedBy != null && !lockedBy.isBlank()) ? lockedBy.trim() : "SYSTEM";
        PayrollPeriodLock lock = new PayrollPeriodLock(salaryPeriodKey, by);
        lockRepo.save(lock);
        return new PayrollPeriodLockDTO(salaryPeriodKey, true, lock.getLockedBy(), lock.getLockedAt());
    }

    @Override
    public boolean isPeriodLocked(String salaryPeriodKey) {
        return lockRepo.existsBySalaryPeriodKey(salaryPeriodKey);
    }
}
