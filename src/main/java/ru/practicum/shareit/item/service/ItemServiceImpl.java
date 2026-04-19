package ru.practicum.shareit.item.service;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dal.BookingRepository;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.notfound.ItemNotFoundException;
import ru.practicum.shareit.exception.notfound.UserNotFoundException;
import ru.practicum.shareit.exception.validation.ValidationException;
import ru.practicum.shareit.item.dal.CommentRepository;
import ru.practicum.shareit.item.dal.ItemRepository;
import ru.practicum.shareit.item.dto.request.CommentDto;
import ru.practicum.shareit.item.dto.request.ItemDto;
import ru.practicum.shareit.item.dto.request.ItemUpdateDto;
import ru.practicum.shareit.item.dto.response.CommentResponseDto;
import ru.practicum.shareit.item.dto.response.ItemResponseDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dal.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public ItemResponseDto createItem(ItemDto itemDto, long userId) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден: " + userId));

        Item item = ItemMapper.toEntity(itemDto);
        item.setOwner(owner);

        return ItemMapper.toResponseDto(itemRepository.save(item));
    }

    @Override
    @Transactional
    public ItemResponseDto updateItem(ItemUpdateDto itemDto, long id, long userId) {
        if (!itemRepository.existsByItemIdAndOwnerId(id, userId)) {
            throw new ItemNotFoundException("item с id = " + id + " и userId = " + userId + " не найден");
        }

        if (itemDto.getName() != null) {
            itemRepository.updateName(itemDto.getName());
        }

        if (itemDto.getDescription() != null) {
            itemRepository.updateDescription(itemDto.getDescription());
        }

        if (itemDto.getAvailable() != null) {
            itemRepository.updateAvailable(itemDto.getAvailable());
        }

        return ItemMapper.toResponseDto(itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("item с id = " + id + " не найден")));
    }

    @Override
    @Transactional
    public ItemResponseDto getItem(long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("item с id = " + id + " не найден"));

        ItemResponseDto dto = ItemMapper.toResponseDto(item);

        List<CommentResponseDto> comments = commentRepository.findByItem_ItemId(id)
                .stream()
                .map(CommentMapper::toDto)
                .toList();
        dto.setComments(comments);

        return dto;
    }

    @Override
    @Transactional
    public List<ItemResponseDto> getItems(long userId) {
        List<Item> items = itemRepository.findItemsByOwnerId(userId);
        List<Long> itemIds = items.stream().map(Item::getItemId).toList();

        LocalDateTime now = LocalDateTime.now();

        Map<Long, LocalDateTime> lastBookings = bookingRepository.findLastBookingsByItemIds(itemIds, now)
                .stream()
                .collect(Collectors.toMap(
                        b -> b.getItem().getItemId(),
                        Booking::getEnd,
                        (existing, replacement) -> existing
                ));

        Map<Long, LocalDateTime> nextBookings = bookingRepository.findNextBookingsByItemIds(itemIds, now)
                .stream()
                .collect(Collectors.toMap(
                        b -> b.getItem().getItemId(),
                        Booking::getStart,
                        (existing, replacement) -> existing
                ));

        Map<Long, List<CommentResponseDto>> commentsByItem = commentRepository.findByItem_ItemIdIn(itemIds)
                .stream()
                .collect(Collectors.groupingBy(
                        c -> c.getItem().getItemId(),
                        Collectors.mapping(CommentMapper::toDto, Collectors.toList())
                ));

        return items.stream()
                .map(item -> {
                    ItemResponseDto dto = ItemMapper.toResponseDto(item);
                    dto.setLastBooking(lastBookings.get(item.getItemId()));
                    dto.setNextBooking(nextBookings.get(item.getItemId()));
                    dto.setComments(commentsByItem.getOrDefault(item.getItemId(), List.of()));
                    return dto;
                })
                .toList();
    }

    @Override
    @Transactional
    public List<ItemResponseDto> searchItem(String text) {
        if (text == null || text.isEmpty()) {
            return List.of();
        }

        return itemRepository.searchByText(text).stream()
                .map(ItemMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional
    public CommentResponseDto addComment(long userId, long itemId, CommentDto commentDto) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден: " + userId));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("item с id = " + itemId + " не найден"));

        boolean hasPastBooking = bookingRepository.existsPastBookingByItemAndUser(
                itemId, userId, Status.APPROVED, LocalDateTime.now());
        if (!hasPastBooking) {
            throw new ValidationException("Пользователь не брал вещь в аренду или аренда ещё не завершена");
        }

        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());

        return CommentMapper.toDto(commentRepository.save(comment));
    }
}