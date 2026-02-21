package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemShortResponseDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestExtendedResponseDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceTest {

    @Mock
    private ItemRequestRepository itemRequestRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private ItemRequestMapper itemRequestMapper;
    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private ItemRequestService itemRequestService;

    private User requestor;
    private ItemRequest itemRequest;
    private ItemRequestCreateDto createDto;
    private ItemRequestResponseDto responseDto;
    private ItemRequestExtendedResponseDto extendedResponseDto;
    private Item item;
    private ItemShortResponseDto itemShortDto;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        requestor = new User();
        requestor.setId(1L);
        requestor.setName("Requestor");
        requestor.setEmail("requestor@example.com");

        itemRequest = new ItemRequest();
        itemRequest.setId(1L);
        itemRequest.setDescription("Нужна дрель");
        itemRequest.setRequestor(requestor);
        itemRequest.setCreated(now);

        createDto = new ItemRequestCreateDto();
        createDto.setDescription("Нужна дрель");

        responseDto = ItemRequestResponseDto.builder()
                .id(1L)
                .description("Нужна дрель")
                .created(now)
                .build();

        item = new Item();
        item.setId(1L);
        item.setName("Дрель");
        item.setDescription("Аккумуляторная дрель");
        item.setAvailable(true);
        item.setOwner(requestor);
        item.setRequest(itemRequest);

        itemShortDto = ItemShortResponseDto.builder()
                .id(1L)
                .name("Дрель")
                .description("Аккумуляторная дрель")
                .build();


        extendedResponseDto = ItemRequestExtendedResponseDto.builder()
                .id(1L)
                .description("Нужна дрель")
                .created(now)
                .items(List.of(itemShortDto))
                .build();
    }

    @Test
    void createItemRequest_whenUserExists_shouldCreateRequest() {
        Long requestorId = 1L;

        when(userRepository.findById(requestorId)).thenReturn(Optional.of(requestor));
        when(itemRequestMapper.mapToItemRequest(createDto)).thenReturn(itemRequest);
        when(itemRequestRepository.save(any(ItemRequest.class))).thenReturn(itemRequest);
        when(itemRequestMapper.mapToResponseDto(itemRequest)).thenReturn(responseDto);

        ItemRequestResponseDto result = itemRequestService.createItemRequest(requestorId, createDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Нужна дрель", result.getDescription());
        assertNotNull(result.getCreated());

        verify(userRepository, times(1)).findById(requestorId);
        verify(itemRequestMapper, times(1)).mapToItemRequest(createDto);
        verify(itemRequestRepository, times(1)).save(any(ItemRequest.class));
        verify(itemRequestMapper, times(1)).mapToResponseDto(itemRequest);
    }

    @Test
    void createItemRequest_whenUserNotFound_shouldThrowNotFoundException() {
        Long requestorId = 99L;

        when(itemRequestMapper.mapToItemRequest(createDto)).thenReturn(itemRequest); // <-- Добавить этот when
        when(userRepository.findById(requestorId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemRequestService.createItemRequest(requestorId, createDto));

        assertEquals("User with id=99 not found", exception.getMessage());

        verify(userRepository, times(1)).findById(requestorId);
        verify(itemRequestMapper, times(1)).mapToItemRequest(createDto);
        verify(itemRequestRepository, never()).save(any());
    }

    @Test
    void getAllItemRequest_whenRequestsExist_shouldReturnList() {
        Long userId = 1L;
        List<ItemRequest> requests = List.of(itemRequest);

        when(itemRequestRepository.findAllByRequestorIdNot(userId)).thenReturn(requests);
        when(itemRequestMapper.mapToResponseDto(itemRequest)).thenReturn(responseDto);

        List<ItemRequestResponseDto> result = itemRequestService.getAllItemRequest(userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("Нужна дрель", result.get(0).getDescription());

        verify(itemRequestRepository, times(1)).findAllByRequestorIdNot(userId);
        verify(itemRequestMapper, times(1)).mapToResponseDto(itemRequest);
    }

    @Test
    void getAllItemRequest_whenNoRequests_shouldReturnEmptyList() {
        Long userId = 1L;

        when(itemRequestRepository.findAllByRequestorIdNot(userId)).thenReturn(List.of());

        List<ItemRequestResponseDto> result = itemRequestService.getAllItemRequest(userId);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(itemRequestRepository, times(1)).findAllByRequestorIdNot(userId);
        verify(itemRequestMapper, never()).mapToResponseDto(any());
    }

    @Test
    void getAllRequestorItemRequests_whenUserExistsAndRequestsExist_shouldReturnExtendedList() {
        Long requestorId = 1L;
        List<ItemRequest> requests = List.of(itemRequest);
        List<Item> items = List.of(item);

        when(userRepository.existsById(requestorId)).thenReturn(true);
        when(itemRequestRepository.findByRequestor_IdOrderByCreatedDesc(requestorId)).thenReturn(requests);
        when(itemRepository.findByRequestorIdWithDetails(requestorId)).thenReturn(items);
        when(itemMapper.mapToShortResponseDtoForList(anyList())).thenReturn(List.of(itemShortDto));

        List<ItemRequestExtendedResponseDto> result = itemRequestService.getAllRequestorItemRequests(requestorId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("Нужна дрель", result.get(0).getDescription());
        assertNotNull(result.get(0).getItems());
        assertEquals(1, result.get(0).getItems().size());

        verify(userRepository, times(1)).existsById(requestorId);
        verify(itemRequestRepository, times(1)).findByRequestor_IdOrderByCreatedDesc(requestorId);
        verify(itemRepository, times(1)).findByRequestorIdWithDetails(requestorId);
        verify(itemMapper, times(1)).mapToShortResponseDtoForList(anyList());
    }

    @Test
    void getAllRequestorItemRequests_whenUserExistsButNoRequests_shouldReturnEmptyList() {
        Long requestorId = 1L;

        when(userRepository.existsById(requestorId)).thenReturn(true);
        when(itemRequestRepository.findByRequestor_IdOrderByCreatedDesc(requestorId)).thenReturn(List.of());

        List<ItemRequestExtendedResponseDto> result = itemRequestService.getAllRequestorItemRequests(requestorId);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(userRepository, times(1)).existsById(requestorId);
        verify(itemRequestRepository, times(1)).findByRequestor_IdOrderByCreatedDesc(requestorId);
        verify(itemRepository, never()).findByRequestorIdWithDetails(any());
        verify(itemMapper, never()).mapToShortResponseDtoForList(any());
    }

    @Test
    void getAllRequestorItemRequests_whenUserNotFound_shouldThrowNotFoundException() {
        Long requestorId = 99L;

        when(userRepository.existsById(requestorId)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemRequestService.getAllRequestorItemRequests(requestorId));

        assertEquals("User with id=99 not found", exception.getMessage());

        verify(userRepository, times(1)).existsById(requestorId);
        verify(itemRequestRepository, never()).findByRequestor_IdOrderByCreatedDesc(any());
        verify(itemRepository, never()).findByRequestorIdWithDetails(any());
    }

    @Test
    void getRequestById_whenUserExistsAndRequestExists_shouldReturnExtendedResponse() {
        Long requestId = 1L;
        Long userId = 1L;
        List<Item> items = List.of(item);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.of(itemRequest));
        when(itemRepository.findByRequest_Id(requestId)).thenReturn(items);
        when(itemMapper.mapToShortResponseDtoForList(items)).thenReturn(List.of(itemShortDto));

        ItemRequestExtendedResponseDto result = itemRequestService.getRequestById(requestId, userId);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Нужна дрель", result.getDescription());
        assertNotNull(result.getItems());
        assertEquals(1, result.getItems().size());

        verify(userRepository, times(1)).existsById(userId);
        verify(itemRequestRepository, times(1)).findById(requestId);
        verify(itemRepository, times(1)).findByRequest_Id(requestId);
        verify(itemMapper, times(1)).mapToShortResponseDtoForList(items);
    }

    @Test
    void getRequestById_whenUserExistsButRequestNotFound_shouldThrowNotFoundException() {
        Long requestId = 99L;
        Long userId = 1L;

        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemRequestService.getRequestById(requestId, userId));

        assertEquals("Запрос не найден", exception.getMessage());

        verify(userRepository, times(1)).existsById(userId);
        verify(itemRequestRepository, times(1)).findById(requestId);
        verify(itemRepository, never()).findByRequest_Id(any());
    }

    @Test
    void getRequestById_whenUserNotFound_shouldThrowNotFoundException() {
        Long requestId = 1L;
        Long userId = 99L;

        when(userRepository.existsById(userId)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemRequestService.getRequestById(requestId, userId));

        assertEquals("User with id=99 not found", exception.getMessage());

        verify(userRepository, times(1)).existsById(userId);
        verify(itemRequestRepository, never()).findById(any());
        verify(itemRepository, never()).findByRequest_Id(any());
    }

    @Test
    void getRequestById_whenUserExistsAndRequestExistsButNoItems_shouldReturnExtendedResponseWithEmptyItems() {
        Long requestId = 1L;
        Long userId = 1L;
        List<Item> emptyItems = List.of();

        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.of(itemRequest));
        when(itemRepository.findByRequest_Id(requestId)).thenReturn(emptyItems);
        when(itemMapper.mapToShortResponseDtoForList(emptyItems)).thenReturn(List.of());

        ItemRequestExtendedResponseDto result = itemRequestService.getRequestById(requestId, userId);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Нужна дрель", result.getDescription());
        assertNotNull(result.getItems());
        assertTrue(result.getItems().isEmpty());

        verify(userRepository, times(1)).existsById(userId);
        verify(itemRequestRepository, times(1)).findById(requestId);
        verify(itemRepository, times(1)).findByRequest_Id(requestId);
        verify(itemMapper, times(1)).mapToShortResponseDtoForList(emptyItems);
    }
}