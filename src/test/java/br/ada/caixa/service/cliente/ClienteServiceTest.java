package br.ada.caixa.service.cliente;
import br.ada.caixa.dto.request.RegistrarClientePFRequestDto;
import br.ada.caixa.dto.request.RegistrarClientePJRequestDto;
import br.ada.caixa.dto.response.ClienteResponseDto;
import br.ada.caixa.dto.response.RegistrarClienteResponseDto;
import br.ada.caixa.entity.Cliente;
import br.ada.caixa.entity.Conta;
import br.ada.caixa.entity.TipoCliente;
import br.ada.caixa.enums.StatusCliente;
import br.ada.caixa.respository.ClienteRepository;
import br.ada.caixa.respository.ContaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private ContaRepository contaRepository;
    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ClienteService service;


    @Test
    @DisplayName("Dado um cliente PF, criar novo e salvar no banco")
    void registrarPFTest(){
        //given
        final RegistrarClientePFRequestDto request = mock(RegistrarClientePFRequestDto.class);
        final Cliente cliente = mock(Cliente.class);
        final Conta conta = mock(Conta.class);
        final String numeroDoc = "0123456789";
        final BigDecimal saldo = BigDecimal.ZERO;
        given(cliente.getDocumento()).willReturn(numeroDoc);
        given(conta.getSaldo()).willReturn(saldo);
        given(modelMapper.map(request, Cliente.class)).willReturn(cliente);
        //when
        when(contaRepository.save(any(Conta.class))).thenReturn(conta);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);
        RegistrarClienteResponseDto clienteRetornado = service.registrarPF(request);
        //then
        verify(clienteRepository).save(cliente);
        assertEquals(numeroDoc,clienteRetornado.getDocumento());
        assertEquals(saldo,clienteRetornado.getSaldoResponseDto().getSaldo());

    }

    @Test
    @DisplayName("Dado um cliente PJ, criar novo e salvar no banco")
    void registrarPJTest() {
        //given
        final RegistrarClientePJRequestDto request = mock(RegistrarClientePJRequestDto.class);
        final Cliente cliente = mock(Cliente.class);
        final Conta conta = mock(Conta.class);
        final String numeroDoc = "0123456789000110";
        final BigDecimal saldo = BigDecimal.ZERO;
        given(cliente.getDocumento()).willReturn(numeroDoc);
        given(conta.getSaldo()).willReturn(saldo);
        given(modelMapper.map(request, Cliente.class)).willReturn(cliente);
        //when
        when(contaRepository.save(any(Conta.class))).thenReturn(conta);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);
        RegistrarClienteResponseDto clienteRetornado = service.registrarPJ(request);
        //then
        verify(clienteRepository).save(cliente);
        assertEquals(numeroDoc, clienteRetornado.getDocumento());
        assertEquals(saldo, clienteRetornado.getSaldoResponseDto().getSaldo());
    }
        @Test
        @DisplayName("Dado clientes, solicitar uma lista por tipo")
        void listarTodosPorTipoTest(){
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
            final TipoCliente tipo = TipoCliente.PF;
            List<Cliente> clientes = new ArrayList<>();
            clientes.add(cliente);
            given(clienteRepository.findAllByTipo(tipo)).willReturn(clientes);
            //when
            List<ClienteResponseDto> clientesResponse = service.listarTodos(tipo);
            //then
            assertEquals(tipo.toString(),clientesResponse.get(0).getTipo());
        }

    @Test
    @DisplayName("Dado clientes, solicitar uma lista completa")
    void listarTodosTest(){
        //given
        final Cliente cliente1 = Cliente
                .builder()
                .tipo(TipoCliente.PF)
                .nome("nome do cliente 1")
                .documento("0123456789")
                .status(StatusCliente.ATIVO)
                .dataNascimento(LocalDate.now())
                .createdAt(LocalDate.now())
                .build();
        final Cliente cliente2 = Cliente
                .builder()
                .tipo(TipoCliente.PJ)
                .nome("nome do cliente 2")
                .documento("01234567890")
                .status(StatusCliente.ATIVO)
                .dataNascimento(LocalDate.now())
                .createdAt(LocalDate.now())
                .build();
        List<Cliente> clientes = new ArrayList<>();
        clientes.add(cliente1);
        clientes.add(cliente2);
        given(clienteRepository.findAll()).willReturn(clientes);
        //when
        List<ClienteResponseDto> clientesResponse = service.listarTodos();
        //then
        assertEquals(clientes.size(),clientesResponse.size());
    }


}
