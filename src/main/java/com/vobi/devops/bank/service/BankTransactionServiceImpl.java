package com.vobi.devops.bank.service;

import java.sql.Timestamp;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.vobi.devops.bank.domain.Account;
import com.vobi.devops.bank.domain.Transaction;
import com.vobi.devops.bank.domain.TransactionType;
import com.vobi.devops.bank.domain.Users;
import com.vobi.devops.bank.dto.DepositDTO;
import com.vobi.devops.bank.dto.TransactionResultDTO;
import com.vobi.devops.bank.dto.TransferDTO;
import com.vobi.devops.bank.dto.WithdrawDTO;
import com.vobi.devops.bank.exception.ZMessManager;

@Service
@Scope("singleton")
public class BankTransactionServiceImpl implements BankTransactionService {

	private final static Double COSTO = 2000.0;

	@Autowired
	AccountService accountService;

	@Autowired
	UsersService userService;

	@Autowired
	TransactionTypeService transactionTypeService;

	@Autowired
	TransactionService transactionService;

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public TransactionResultDTO transfer(TransferDTO transferDTO) throws Exception {

		WithdrawDTO withdrawDTO = new WithdrawDTO(transferDTO.getAccoIdOrigin(), transferDTO.getAmount(),
				transferDTO.getUserEmail());
		withdraw(withdrawDTO);

		DepositDTO depositDTO = new DepositDTO(transferDTO.getAccoIdDestination(), transferDTO.getAmount(),
				transferDTO.getUserEmail());
		deposit(depositDTO);

		withdrawDTO = new WithdrawDTO(transferDTO.getAccoIdOrigin(), COSTO, transferDTO.getUserEmail());
		TransactionResultDTO withdrawResult = withdraw(withdrawDTO);

		depositDTO = new DepositDTO("9999-9999-9999-9999", COSTO, transferDTO.getUserEmail());
		deposit(depositDTO);

		Optional<TransactionType> transactionType3 = transactionTypeService.findById(3);
		if (!transactionType3.isPresent()) {
			throw (new ZMessManager()).new FindingException("tipo de transacción 3");
		}
		TransactionType transactionType = transactionType3.get();

		Optional<Account> accountOptional = accountService.findById(transferDTO.getAccoIdOrigin());
		if (!accountOptional.isPresent()) {
			throw (new ZMessManager()).new FindingException("cuenta con id " + transferDTO.getAccoIdOrigin());
		}

		Account account = accountOptional.get();

		Optional<Users> userOptional = userService.findById(transferDTO.getUserEmail());
		if (!userOptional.isPresent()) {
			throw (new ZMessManager()).new FindingException("Usuario con id " + transferDTO.getUserEmail());
		}

		Users user = userOptional.get();

		Transaction transaction = new Transaction();
		transaction.setAccount(account);
		transaction.setAmount(transferDTO.getAmount());
		transaction.setDate(new Timestamp(System.currentTimeMillis()));
		transaction.setTranId(null);
		transaction.setTransactionType(transactionType);
		transaction.setUsers(user);

		transactionService.save(transaction);

		return new TransactionResultDTO(transaction.getTranId(), withdrawResult.getBalance());

	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public TransactionResultDTO withdraw(WithdrawDTO withdrawDTO) throws Exception {

		if (withdrawDTO == null) {
			throw new Exception("El WithdrawDTO es nulo");
		}

		if (withdrawDTO.getAccoId() == null || withdrawDTO.getAccoId().trim().isEmpty() == true) {
			throw new Exception("El AccoId es obligatorio");
		}

		if (withdrawDTO.getAmount() == null || withdrawDTO.getAmount() <= 0) {
			throw new Exception("El Amount es obligatorio y debe ser mayor que cero");
		}

		if (withdrawDTO.getUserEmail() == null || withdrawDTO.getUserEmail().trim().isEmpty() == true) {
			throw new Exception("El UserEmail es obligatorio");
		}

		if (accountService.findById(withdrawDTO.getAccoId()).isPresent() == false) {
			throw new ZMessManager().new AccountNotFoundException(withdrawDTO.getAccoId());
		}

		Account account = accountService.findById(withdrawDTO.getAccoId()).get();

		if (account.getEnable().trim().equals("N") == true) {
			throw new ZMessManager().new AccountNotEnableException(withdrawDTO.getAccoId());
		}

		if (userService.findById(withdrawDTO.getUserEmail()).isPresent() == false) {
			throw new ZMessManager().new UserNotFoundException(withdrawDTO.getUserEmail());
		}

		Users user = userService.findById(withdrawDTO.getUserEmail()).get();

		if (user.getEnable().trim().equals("N") == true) {
			throw new ZMessManager().new UserDisableException(withdrawDTO.getUserEmail());
		}

		TransactionType transactionType = transactionTypeService.findById(1).get();

		Transaction transaction = new Transaction();
		transaction.setAccount(account);
		transaction.setAmount(withdrawDTO.getAmount());
		transaction.setDate(new Timestamp(System.currentTimeMillis()));
		transaction.setTranId(null);
		transaction.setTransactionType(transactionType);
		transaction.setUsers(user);

		Double nuevoSaldo = account.getBalance() - withdrawDTO.getAmount();
		account.setBalance(nuevoSaldo);

		transaction = transactionService.save(transaction);
		accountService.update(account);

		TransactionResultDTO transactionResultDTO = new TransactionResultDTO(transaction.getTranId(), nuevoSaldo);

		return transactionResultDTO;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public TransactionResultDTO deposit(DepositDTO depositDTO) throws Exception {
		
		if (depositDTO == null) {
			throw new Exception("El depositDTO es nulo");
		}

		if (depositDTO.getAccoId() == null || depositDTO.getAccoId().trim().isEmpty() == true) {
			throw new Exception("El AccoId es obligatorio");
		}

		if (depositDTO.getAmount() == null || depositDTO.getAmount() < 0) {
			throw new Exception("El Amount es obligatorio y debe ser mayor que cero");
		}

		if (depositDTO.getUserEmail() == null || depositDTO.getUserEmail().trim().isEmpty() == true) {
			throw new Exception("El UserEmail es obligatorio");
		}

		if (accountService.findById(depositDTO.getAccoId()).isPresent() == false) {
			throw new ZMessManager().new AccountNotFoundException(depositDTO.getAccoId());
		}

		Account account = accountService.findById(depositDTO.getAccoId()).get();
		
		if (account.getEnable().trim().equals("N") == true) {
			throw new ZMessManager().new AccountNotEnableException(depositDTO.getAccoId());
		}

		if (userService.findById(depositDTO.getUserEmail()).isPresent() == false) {
			throw new ZMessManager().new UserNotFoundException(depositDTO.getUserEmail());
		}

		Users user = userService.findById(depositDTO.getUserEmail()).get();

		if (user.getEnable().trim().equals("N") == true) {
			throw new ZMessManager().new UserDisableException(depositDTO.getUserEmail());
		}

		TransactionType transactionType = transactionTypeService.findById(2).get();

		Transaction transaction = new Transaction();
		transaction.setAccount(account);
		transaction.setAmount(depositDTO.getAmount());
		transaction.setDate(new Timestamp(System.currentTimeMillis()));
		transaction.setTransactionType(transactionType);
		transaction.setUsers(user);

		Double nuevoSaldo = account.getBalance() + depositDTO.getAmount();
		account.setBalance(nuevoSaldo);

		transaction = transactionService.save(transaction);
		accountService.update(account);

		TransactionResultDTO transactionResultDTO = new TransactionResultDTO(transaction.getTranId(), nuevoSaldo);
		
		return transactionResultDTO;
		
	}

}
