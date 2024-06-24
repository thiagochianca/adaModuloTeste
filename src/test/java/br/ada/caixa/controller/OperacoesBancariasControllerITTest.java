package br.ada.caixa.controller;

import br.ada.caixa.dto.request.DepositoRequestDto;
import br.ada.caixa.dto.request.InvestimentoRequestDto;
import br.ada.caixa.dto.request.SaqueRequestDto;
import br.ada.caixa.dto.request.TransferenciaRequestDto;
import br.ada.caixa.dto.response.SaldoResponseDto;
import br.ada.caixa.entity.Cliente;
import br.ada.caixa.entity.Conta;
import br.ada.caixa.entity.TipoCliente;
import br.ada.caixa.entity.TipoConta;
import br.ada.caixa.enums.StatusCliente;
import br.ada.caixa.exceptions.ValidacaoException;
import br.ada.caixa.respository.ClienteRepository;
import br.ada.caixa.respository.ContaRepository;
import br.ada.caixa.service.conta.ContaService;
import br.ada.caixa.service.operacoesbancarias.deposito.DepositoService;
import br.ada.caixa.service.operacoesbancarias.investimento.InvestimentoService;
import br.ada.caixa.service.operacoesbancarias.saldo.SaldoService;
import br.ada.caixa.service.operacoesbancarias.saque.SaqueService;
import br.ada.caixa.service.operacoesbancarias.transferencia.TransferenciaService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(MockitoExtension.class)
class OperacoesBancariasControllerITTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @SpyBean
    private ContaRepository contaRepository;
    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    DepositoService depositoService;
    @Autowired
    SaqueService saqueService;
    @Autowired
    TransferenciaService transferenciaService;
    @Autowired
    SaldoService saldoService;
    @Autowired
    InvestimentoService investimentoService;
    @Autowired
    ContaService contaService;

    private String url;

    @BeforeEach
    void setUp() {
        //SET URL
        url = "http://localhost:" + port + "/operacoes";

        //CRIAR CLIENTES
        var cliente1 = Cliente.builder()
                .documento("123456889")
                .nome("Teste 1")
                .dataNascimento(LocalDate.now())
                .status(StatusCliente.ATIVO)
                .tipo(TipoCliente.PF)
                .createdAt(LocalDate.now())
                .build();
        var cliente2 = Cliente.builder()
                .documento("1234567891")
                .nome("Teste 2")
                .dataNascimento(LocalDate.now())
                .status(StatusCliente.ATIVO)
                .tipo(TipoCliente.PF)
                .createdAt(LocalDate.now())
                .build();

        clienteRepository.saveAllAndFlush(List.of(cliente1, cliente2));

        //CRIAR CONTAS
        var contaCorrente1 = Conta.builder()
                .numero(1L)
                .saldo(BigDecimal.ZERO)
                .tipo(TipoConta.CONTA_CORRENTE)
//                .cliente(clienteRepository.findByDocumento(cliente1.getDocumento()).get())
                .cliente(cliente1)
                .createdAt(LocalDate.now())
                .build();

        var contaCorrente2 = Conta.builder()
                .numero(2L)
                .saldo(BigDecimal.ZERO)
                .tipo(TipoConta.CONTA_CORRENTE)
//                .cliente(clienteRepository.findByDocumento(cliente2.getDocumento()).get())
                .cliente(cliente2)
                .createdAt(LocalDate.now())
                .build();

        contaRepository.saveAllAndFlush(List.of(contaCorrente1, contaCorrente2));

    }

    @AfterEach
    void tearDown() {
        contaRepository.deleteAllInBatch();
        clienteRepository.deleteAllInBatch();
    }

    @Test
    void depositarTest() {
        //given
        final var valor = BigDecimal.valueOf(100.50);
        final var numeroConta = 1L;

        DepositoRequestDto depositoRequestDto =
                DepositoRequestDto.builder()
                        .numeroConta(numeroConta)
                        .valor(valor)
                        .build();
        //when
        var response = restTemplate.postForEntity(url + "/depositar", depositoRequestDto, Void.class);

        //then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(valor.compareTo(contaRepository.findByNumero(numeroConta).get().getSaldo())).isZero();
        assertEquals(0, valor.compareTo(contaRepository.findByNumero(numeroConta).get().getSaldo()));

        verify(contaRepository).save(any(Conta.class));
    }

    @Test
    void sacar() {
        //given
        final var valor = BigDecimal.valueOf(10);
        final var numeroConta = 1L;

        SaqueRequestDto saqueRequestDto =
                SaqueRequestDto.builder()
                        .numeroConta(numeroConta)
                        .valor(valor)
                        .build();

        Conta conta = contaRepository.findByNumero(numeroConta).get();
        conta.setSaldo(BigDecimal.valueOf(10));
        contaRepository.saveAndFlush(conta);
        BigDecimal saldo = conta.getSaldo().subtract(valor);

        //when

        var response = restTemplate.postForEntity(url + "/sacar", saqueRequestDto, Void.class);

        //then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(saldo.compareTo(contaRepository.findByNumero(numeroConta).get().getSaldo())).isZero();
        assertEquals(0, saldo.compareTo(contaRepository.findByNumero(numeroConta).get().getSaldo()));
        verify(contaRepository).save(any(Conta.class));
    }

    @Test
    void transferencia() {
        //given
        final var valor = BigDecimal.valueOf(10);
        final var numeroConta1 = 1L;
        final var numeroConta2 = 2L;

        TransferenciaRequestDto request =
                TransferenciaRequestDto.builder()
                        .numeroContaOrigem(numeroConta1)
                        .numeroContaDestino(numeroConta2)
                        .valor(valor)
                        .build();

        Conta conta1 = contaRepository.findByNumero(numeroConta1).get();
        Conta conta2 = contaRepository.findByNumero(numeroConta2).get();
        conta1.setSaldo(BigDecimal.valueOf(100));
        contaRepository.saveAllAndFlush(List.of(conta1,conta2));
        BigDecimal saldoConta1 = conta1.getSaldo().subtract(valor);
        BigDecimal saldoConta2 = conta2.getSaldo().add(valor);

        //when

        var response = restTemplate.postForEntity(url + "/transferir", request, Void.class);

        //then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertThat(saldoConta1.compareTo(contaRepository.findByNumero(numeroConta1).get().getSaldo())).isZero();
        assertThat(saldoConta2.compareTo(contaRepository.findByNumero(numeroConta2).get().getSaldo())).isZero();
        verify(contaRepository, atLeast(2)).save(any(Conta.class));
    }

    @Test
    void consultarSaldoTeste() {
        //given
        final var numeroConta = 1L;
        //when
        var response = restTemplate.getForObject(url +
                "/saldo/" + numeroConta, SaldoResponseDto.class);
        //then
        assertNotNull(response);
        assertEquals(numeroConta, response.getNumeroConta());
        assertEquals(0, BigDecimal.ZERO.compareTo(response.getSaldo()));
    }

    @Test
    void investimentoTeste() {
        final var documento = "123456889";
        final var valor = BigDecimal.valueOf(100);
        InvestimentoRequestDto request =
                InvestimentoRequestDto.builder()
                        .documentoCliente(documento)
                        .valor(valor)
                        .build();
        Cliente cliente = clienteRepository.findByDocumento(documento).get();
        TipoCliente tipo = cliente.getTipo();
        BigDecimal novoSaldo;
        if (tipo.equals(TipoCliente.PF)) {
            novoSaldo = valor.multiply(BigDecimal.valueOf(1.01));
        } else {
            novoSaldo = valor.multiply(BigDecimal.valueOf(1.02));
        }

        //when
        var response = restTemplate.postForEntity(url + "/investimento", request, SaldoResponseDto.class);
        //then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(novoSaldo, response.getBody().getSaldo());
        assertEquals(documento, contaRepository.findByNumero(response
                .getBody().getNumeroConta()).get().getCliente().getDocumento());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getNumeroConta());
        assertNotNull(response.getBody().getSaldo());
    }
    @Test
    void abrirContaPoupancaTest() {
        final var documento = "123456889";
        //when
        var response = restTemplate.getForObject(url +
                "/abrir-conta-poupanca/" + documento, SaldoResponseDto.class);
        //then
        assertEquals(documento,
                contaRepository.findByNumero(response
                                .getNumeroConta()).get()
                                .getCliente()
                                .getDocumento()
        );
        assertNotNull(response);
        assertEquals(BigDecimal.ZERO, response.getSaldo());
        assertEquals(TipoConta.CONTA_POUPANCA,
                contaRepository.findByNumero(response.
                        getNumeroConta()).get().getTipo());
        assertEquals(TipoCliente.PF,
                contaRepository.findByNumero(response.
                        getNumeroConta()).get().getCliente()
                        .getTipo());
    }
}