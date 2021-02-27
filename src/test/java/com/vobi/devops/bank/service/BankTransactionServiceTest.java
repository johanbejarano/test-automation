package com.vobi.devops.bank.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.vobi.devops.bank.builder.AccountBuilder;
import com.vobi.devops.bank.builder.TransactionTypeBuilder;
import com.vobi.devops.bank.builder.UsersBuilder;
import com.vobi.devops.bank.domain.Account;
import com.vobi.devops.bank.domain.Transaction;
import com.vobi.devops.bank.domain.TransactionType;
import com.vobi.devops.bank.domain.Users;
import com.vobi.devops.bank.dto.DepositDTO;
import com.vobi.devops.bank.dto.TransactionResultDTO;
import com.vobi.devops.bank.exception.ZMessManager.AccountNotEnableException;
import com.vobi.devops.bank.exception.ZMessManager.AccountNotFoundException;
import com.vobi.devops.bank.exception.ZMessManager.UserDisableException;
import com.vobi.devops.bank.exception.ZMessManager.UserNotFoundException;

@ExtendWith(MockitoExtension.class)
class BankTransactionServiceTest {

	@InjectMocks
	private BankTransactionServiceImpl bankTransactionService;

	@Mock
	AccountServiceImpl accountService;

	@Mock
	UsersServiceImpl userService;

	@Mock
	TransactionTypeServiceImpl transactionTypeService;

	@Mock
	TransactionServiceImpl transactionService;
	
	@Nested
	class BankTransactionServiceDepositTest {
		
		@Test
		void debeDepositar_debe_crear_una_transaccion() throws Exception {
			// Arrange
			String accountId = "4640-0341-9387-5781";
			String userEmail = "vondrusek1@wisc.edu";
			Double amount = 15000.0;

			TransactionResultDTO transactionResultDTO = null;

			DepositDTO depositDTO = new DepositDTO(accountId, amount, userEmail);

			Account account = AccountBuilder.getAccount();
			Users user = UsersBuilder.getUsers();
			TransactionType transactionType = TransactionTypeBuilder.getTransactionTypeDeposit();

			Double amountExpected = account.getBalance() + amount;

			when(accountService.findById(accountId)).thenReturn(Optional.ofNullable(account));
			when(userService.findById(userEmail)).thenReturn(Optional.ofNullable(user));
			when(transactionTypeService.findById(2)).thenReturn(Optional.ofNullable(transactionType));

			when(transactionService.save(any(Transaction.class))).then(new Answer<Transaction>() {
				int sequence = 1;

				@Override
				public Transaction answer(InvocationOnMock invocation) throws Throwable {
					Transaction transaction = invocation.getArgument(0);
					transaction.setTranId(sequence);
					
					//Asegurar que se haya modificado los valores de la transacción
					assertAll(
						() -> assertEquals(transaction.getAccount(), account),
						() -> assertEquals(transaction.getAmount(), amount),
						() -> assertNotNull(transaction.getDate()),
						() -> assertNotNull(transaction.getTransactionType()),
						() -> assertNotNull(transaction.getUsers()),
						() -> assertNotNull(transaction.getTranId())
						);
					
					
					return transaction;
				}

			});
			
			when(accountService.update(any(Account.class))).thenReturn(account);

			transactionResultDTO = bankTransactionService.deposit(depositDTO);

			// Assert
			
			assertEquals(amountExpected, transactionResultDTO.getBalance());
		}
		
		@Test
		void debeDepositar_debe_fallar_si_cantidad_es_negativo() throws Exception {
			// Arrange
			String accountId = "4640-0341-9387-5781";
			DepositDTO depositDTO = new DepositDTO(accountId, -1d, "vondrusek1@wisc.edu");
			String messageExpected = "El Amount es obligatorio y debe ser mayor que cero";

			// Act
			Exception exception = assertThrows(Exception.class, () -> {
				bankTransactionService.deposit(depositDTO);
			});

			// Assert
			assertEquals(messageExpected, exception.getMessage());
			
		}
		
		@Test
		void debeLanzarExceptionDepositDTONull() throws Exception {
			// Arrange
			DepositDTO depositDTO = null;
			String messageExpected = "El depositDTO es nulo";

			// Act
			Exception exception = assertThrows(Exception.class, () -> {
				bankTransactionService.deposit(depositDTO);
			});

			// Assert
			assertEquals(messageExpected, exception.getMessage());
		}

		@Test
		void debeLanzarExceptionAccountIdNull() throws Exception {
			// Arrange
			DepositDTO depositDTO = new DepositDTO(null, 15000.0, "vondrusek1@wisc.edu");
			String messageExpected = "El AccoId es obligatorio";

			// Act
			Exception exception = assertThrows(Exception.class, () -> {
				bankTransactionService.deposit(depositDTO);
			});

			// Assert
			assertEquals(messageExpected, exception.getMessage());
		}

		@Test
		void debeLanzarExceptionAmountMenorACero() throws Exception {
			// Arrange
			String accountId = "4640-0341-9387-5781";
			DepositDTO depositDTO = new DepositDTO(accountId, -1.0, "vondrusek1@wisc.edu");
			String messageExpected = "El Amount es obligatorio y debe ser mayor que cero";

			// Act
			Exception exception = assertThrows(Exception.class, () -> {
				bankTransactionService.deposit(depositDTO);
			});

			// Assert
			assertEquals(messageExpected, exception.getMessage());
		}

		@Test
		void debeLanzarExceptionAmountNull() throws Exception {
			// Arrange
			String accountId = "4640-0341-9387-5781";
			DepositDTO depositDTO = new DepositDTO(accountId, null, "vondrusek1@wisc.edu");
			String messageExpected = "El Amount es obligatorio y debe ser mayor que cero";

			// Act
			Exception exception = assertThrows(Exception.class, () -> {
				bankTransactionService.deposit(depositDTO);
			});

			// Assert
			assertEquals(messageExpected, exception.getMessage());
		}

		@Test
		void debeLanzarExceptionUserEmailNull() throws Exception {
			// Arrange
			String accountId = "4640-0341-9387-5781";
			DepositDTO depositDTO = new DepositDTO(accountId, 1500000.0, null);
			String messageExpected = "El UserEmail es obligatorio";

			// Act
			Exception exception = assertThrows(Exception.class, () -> {
				bankTransactionService.deposit(depositDTO);
			});

			// Assert
			assertEquals(messageExpected, exception.getMessage());
		}

		@Test
		void debeLanzarExceptionAccountNotFound() throws Exception {
			// Arrange
			String accountId = "4640-0341-9387-5781";
			DepositDTO depositDTO = new DepositDTO(accountId, 15000.0, "vondrusek1@wisc.edu");
			String messageExpected = "The account with id " + accountId + " was not found";
			when(accountService.findById(accountId)).thenReturn(Optional.ofNullable(null));

			// Act
			Exception exception = assertThrows(AccountNotFoundException.class, () -> {
				bankTransactionService.deposit(depositDTO);
			});
			// Assert

			assertEquals(messageExpected, exception.getMessage());
		}

		@Test
		void debeLanzarExceptionAccountNoActiva() throws Exception {
			// Arrange
			String accountId = "4640-0341-9387-5781";
			DepositDTO depositDTO = new DepositDTO(accountId, 15000.0, "vondrusek1@wisc.edu");
			String messageExpected = "La cuenta con id " + accountId + " no esta activa";
			Account account = AccountBuilder.getAccountDisable();
			when(accountService.findById(accountId)).thenReturn(Optional.ofNullable(account));

			// Act
			Exception exception = assertThrows(AccountNotEnableException.class, () -> {
				bankTransactionService.deposit(depositDTO);
			});
			// Assert

			assertEquals(messageExpected, exception.getMessage());
		}

		@Test
		void debeLanzarExceptionUserNotFound() throws Exception {
			// Arrange
			String accountId = "4640-0341-9387-5781";
			String userEmail = "vondrusek1@wisc.edu";
			Double amount = 15000.0;

			DepositDTO depositDTO = new DepositDTO(accountId, amount, userEmail);
			String messageExpected = "La user con Email " + userEmail + " no esta existe";
			Account account = AccountBuilder.getAccount();

			when(accountService.findById(accountId)).thenReturn(Optional.ofNullable(account));
			when(userService.findById(userEmail)).thenReturn(Optional.ofNullable(null));

			// Act
			Exception exception = assertThrows(UserNotFoundException.class, () -> {
				bankTransactionService.deposit(depositDTO);
			});
			// Assert

			assertEquals(messageExpected, exception.getMessage());
		}

		@Test
		void debeLanzarExceptionUserDisable() throws Exception {
			// Arrange
			String accountId = "4640-0341-9387-5781";
			String userEmail = "vondrusek1@wisc.edu";
			Double amount = 15000.0;

			DepositDTO depositDTO = new DepositDTO(accountId, amount, userEmail);
			String messageExpected = "El user con Email " + userEmail + " no esta activo";
			Account account = AccountBuilder.getAccount();
			Users user = UsersBuilder.getUsersDisable();

			when(accountService.findById(accountId)).thenReturn(Optional.ofNullable(account));
			when(userService.findById(userEmail)).thenReturn(Optional.ofNullable(user));

			// Act
			Exception exception = assertThrows(UserDisableException.class, () -> {
				bankTransactionService.deposit(depositDTO);
			});
			// Assert

			assertEquals(messageExpected, exception.getMessage());
		}
		
	}

	
	

}
