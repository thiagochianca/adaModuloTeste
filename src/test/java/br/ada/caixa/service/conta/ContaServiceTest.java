package br.ada.caixa.service.conta;

import br.ada.caixa.entity.Cliente;
import br.ada.caixa.entity.Conta;
import br.ada.caixa.entity.TipoCliente;
import br.ada.caixa.enums.StatusCliente;
import br.ada.caixa.exceptions.ValidacaoException;
import br.ada.caixa.respository.ClienteRepository;
import br.ada.caixa.respository.ContaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ContaServiceTest {
    @Mock
    private ContaRepository contaRepository;
    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ContaService service;

    @Test
    @DisplayName("Dado um cpf, abrir uma conta")
    void abrirContaPoupancaTest(){
        //given
        final Cliente cliente = Cliente
                .builder()
                .tipo(TipoCliente.PF)
                .nome("nome do cliente")
                .documento("0123456789")
                .status(StatusCliente.ATIVO)
                .dataNascimento(LocalDate.now())
                .createdAt(LocalDate.now())
                .build();
        Conta contaCliente = Conta.builder().cliente(cliente).build();
        final String cpf = "0123456789";
        given(clienteRepository.findByDocumento(cpf)).willReturn(Optional.of(cliente));
        //when
        when(contaRepository.save(any(Conta.class))).thenReturn(contaCliente);
        Conta conta = service.abrirContaPoupanca(cpf);
        //then
        assertEquals(cliente,conta.getCliente());
    }

    @Test
    void abrirContaPoupancaFailsTest() {
        //given
        final String cpf = "0123456789";
        //when
        //then
        assertThrows(ValidacaoException.class,
                () -> service.abrirContaPoupanca(cpf));
    }
}
