package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemRepositoryImplTest {

    private ItemRepositoryImpl itemRepository;
    private User testOwner;

    @BeforeEach
    void setUp() throws Exception {
        itemRepository = new ItemRepositoryImpl();
        resetStaticId();

        testOwner = User.builder()
                .id(1L)
                .name("Owner")
                .email("owner@mail.com")
                .build();
    }

    // ---------- saveItem ----------
    @Test
    void shouldSaveItemAndGenerateId() {
        Item item = Item.builder()
                .name("Item")
                .description("Description")
                .available(true)
                .owner(testOwner)
                .build();

        Item saved = itemRepository.saveItem(item);

        assertNotNull(saved.getId());
        assertEquals(1L, saved.getId());
        assertEquals("Item", saved.getName());
        assertEquals("Description", saved.getDescription());
        assertTrue(saved.getAvailable());
        assertEquals(testOwner, saved.getOwner());
    }

    @Test
    void shouldGenerateSequentialIds() {
        Item item1 = Item.builder()
                .name("Item1")
                .description("Desc1")
                .available(true)
                .owner(testOwner)
                .build();

        Item item2 = Item.builder()
                .name("Item2")
                .description("Desc2")
                .available(true)
                .owner(testOwner)
                .build();

        Item saved1 = itemRepository.saveItem(item1);
        Item saved2 = itemRepository.saveItem(item2);

        assertEquals(1L, saved1.getId());
        assertEquals(2L, saved2.getId());
    }

    @Test
    void shouldUpdateExistingItemWhenIdIsPresent() {
        Item item = Item.builder()
                .name("Original")
                .description("Original Description")
                .available(true)
                .owner(testOwner)
                .build();
        Item saved = itemRepository.saveItem(item);

        saved.setName("Updated");
        saved.setDescription("Updated Description");
        saved.setAvailable(false);
        Item updated = itemRepository.saveItem(saved);

        assertEquals(saved.getId(), updated.getId());
        assertEquals("Updated", updated.getName());
        assertEquals("Updated Description", updated.getDescription());
        assertFalse(updated.getAvailable());
    }

    @Test
    void shouldReturnCopyOfSavedItem() {
        Item item = Item.builder()
                .name("Item")
                .description("Description")
                .available(true)
                .owner(testOwner)
                .build();

        Item saved = itemRepository.saveItem(item);
        saved.setName("Modified");

        Item retrieved = itemRepository.getItemById(saved.getId());
        assertEquals("Item", retrieved.getName());
    }

    // ---------- getItemById ----------
    @Test
    void shouldReturnItemById() {
        Item item = Item.builder()
                .name("Item")
                .description("Description")
                .available(true)
                .owner(testOwner)
                .build();
        Item saved = itemRepository.saveItem(item);

        Item found = itemRepository.getItemById(saved.getId());

        assertNotNull(found);
        assertEquals(saved.getId(), found.getId());
        assertEquals("Item", found.getName());
        assertEquals("Description", found.getDescription());
    }

    @Test
    void shouldReturnNullWhenItemNotExists() {
        Item found = itemRepository.getItemById(999L);

        assertNull(found);
    }

    @Test
    void shouldReturnCopyOfItemOnGet() {
        Item item = Item.builder()
                .name("Item")
                .description("Description")
                .available(true)
                .owner(testOwner)
                .build();
        Item saved = itemRepository.saveItem(item);

        Item found = itemRepository.getItemById(saved.getId());
        found.setName("Modified");

        Item foundAgain = itemRepository.getItemById(saved.getId());
        assertEquals("Item", foundAgain.getName());
    }

    // ---------- getUserItems ----------
    @Test
    void shouldReturnUserItems() {
        User owner1 = User.builder()
                .id(1L)
                .name("Owner1")
                .email("owner1@mail.com")
                .build();

        User owner2 = User.builder()
                .id(2L)
                .name("Owner2")
                .email("owner2@mail.com")
                .build();

        Item item1 = Item.builder()
                .name("Item1")
                .description("Desc1")
                .available(true)
                .owner(owner1)
                .build();

        Item item2 = Item.builder()
                .name("Item2")
                .description("Desc2")
                .available(true)
                .owner(owner1)
                .build();

        Item item3 = Item.builder()
                .name("Item3")
                .description("Desc3")
                .available(true)
                .owner(owner2)
                .build();

        itemRepository.saveItem(item1);
        itemRepository.saveItem(item2);
        itemRepository.saveItem(item3);

        List<Item> owner1Items = itemRepository.getUserItems(1L);
        List<Item> owner2Items = itemRepository.getUserItems(2L);

        assertEquals(2, owner1Items.size());
        assertEquals(1, owner2Items.size());
        assertTrue(owner1Items.stream().allMatch(item -> item.getOwner().getId().equals(1L)));
        assertTrue(owner2Items.stream().allMatch(item -> item.getOwner().getId().equals(2L)));
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoItems() {
        List<Item> items = itemRepository.getUserItems(999L);

        assertTrue(items.isEmpty());
    }

    @Test
    void shouldReturnCopiesInGetUserItems() {
        Item item = Item.builder()
                .name("Item")
                .description("Description")
                .available(true)
                .owner(testOwner)
                .build();
        itemRepository.saveItem(item);

        List<Item> items = itemRepository.getUserItems(1L);
        items.getFirst().setName("Modified");

        List<Item> itemsAgain = itemRepository.getUserItems(1L);
        assertEquals("Item", itemsAgain.getFirst().getName());
    }

    // ---------- searchItems ----------
    @Test
    void shouldSearchItemsByName() {
        Item item1 = Item.builder()
                .name("Drill Power")
                .description("Tool")
                .available(true)
                .owner(testOwner)
                .build();

        Item item2 = Item.builder()
                .name("Hammer")
                .description("Hand tool")
                .available(true)
                .owner(testOwner)
                .build();

        itemRepository.saveItem(item1);
        itemRepository.saveItem(item2);

        List<Item> results = itemRepository.searchItems("drill");

        assertEquals(1, results.size());
        assertEquals("Drill Power", results.getFirst().getName());
    }

    @Test
    void shouldSearchItemsByDescription() {
        Item item = Item.builder()
                .name("Tool")
                .description("Electric drill for professionals")
                .available(true)
                .owner(testOwner)
                .build();

        itemRepository.saveItem(item);

        List<Item> results = itemRepository.searchItems("electric");

        assertEquals(1, results.size());
        assertEquals("Tool", results.getFirst().getName());
    }

    @Test
    void shouldSearchCaseInsensitive() {
        Item item = Item.builder()
                .name("Drill")
                .description("Power tool")
                .available(true)
                .owner(testOwner)
                .build();

        itemRepository.saveItem(item);

        List<Item> results1 = itemRepository.searchItems("DRILL");
        List<Item> results2 = itemRepository.searchItems("drill");
        List<Item> results3 = itemRepository.searchItems("DrIlL");

        assertEquals(1, results1.size());
        assertEquals(1, results2.size());
        assertEquals(1, results3.size());
    }

    @Test
    void shouldNotReturnUnavailableItems() {
        Item available = Item.builder()
                .name("Available Drill")
                .description("Available")
                .available(true)
                .owner(testOwner)
                .build();

        Item unavailable = Item.builder()
                .name("Unavailable Drill")
                .description("Not available")
                .available(false)
                .owner(testOwner)
                .build();

        itemRepository.saveItem(available);
        itemRepository.saveItem(unavailable);

        List<Item> results = itemRepository.searchItems("drill");

        assertEquals(1, results.size());
        assertEquals("Available Drill", results.getFirst().getName());
    }

    @Test
    void shouldReturnEmptyListWhenQueryIsNull() {
        Item item = Item.builder()
                .name("Item")
                .description("Description")
                .available(true)
                .owner(testOwner)
                .build();
        itemRepository.saveItem(item);

        List<Item> results = itemRepository.searchItems(null);

        assertTrue(results.isEmpty());
    }

    @Test
    void shouldReturnEmptyListWhenQueryIsBlank() {
        Item item = Item.builder()
                .name("Item")
                .description("Description")
                .available(true)
                .owner(testOwner)
                .build();
        itemRepository.saveItem(item);

        List<Item> results1 = itemRepository.searchItems("");
        List<Item> results2 = itemRepository.searchItems("   ");

        assertTrue(results1.isEmpty());
        assertTrue(results2.isEmpty());
    }

    @Test
    void shouldReturnEmptyListWhenNoMatches() {
        Item item = Item.builder()
                .name("Drill")
                .description("Tool")
                .available(true)
                .owner(testOwner)
                .build();
        itemRepository.saveItem(item);

        List<Item> results = itemRepository.searchItems("nonexistent");

        assertTrue(results.isEmpty());
    }

    @Test
    void shouldReturnMultipleMatchingItems() {
        Item item1 = Item.builder()
                .name("Electric Drill")
                .description("Power tool")
                .available(true)
                .owner(testOwner)
                .build();

        Item item2 = Item.builder()
                .name("Manual Drill")
                .description("Hand tool")
                .available(true)
                .owner(testOwner)
                .build();

        itemRepository.saveItem(item1);
        itemRepository.saveItem(item2);

        List<Item> results = itemRepository.searchItems("drill");

        assertEquals(2, results.size());
    }

    @Test
    void shouldReturnCopiesInSearchResults() {
        Item item = Item.builder()
                .name("Drill")
                .description("Power tool")
                .available(true)
                .owner(testOwner)
                .build();
        itemRepository.saveItem(item);

        List<Item> results = itemRepository.searchItems("drill");
        results.getFirst().setName("Modified");

        List<Item> resultsAgain = itemRepository.searchItems("drill");
        assertEquals("Drill", resultsAgain.getFirst().getName());
    }

    private void resetStaticId() throws Exception {
        Field idField = ItemRepositoryImpl.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(null, 0L);
    }
}
