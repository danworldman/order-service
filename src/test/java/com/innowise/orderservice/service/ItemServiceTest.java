package com.innowise.orderservice.service;

import com.innowise.orderservice.dao.ItemDAO;
import com.innowise.orderservice.exception.ItemNotFoundException;
import com.innowise.orderservice.mapper.ItemMapper;
import com.innowise.orderservice.model.dto.item.ItemResponse;
import com.innowise.orderservice.service.impl.ItemServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest extends ServiceTestData {

    @Mock
    private ItemDAO itemDAO;

    @Mock
    private ItemMapper itemMapper;


    @InjectMocks
    private ItemServiceImpl itemService;

    @Test
    void getItemById_shouldReturnItemResponse_whenItemExists() {
        Mockito.when(itemDAO.findById(DEFAULT_ITEM_ID)).thenReturn(Optional.of(sampleItem));
        Mockito.when(itemMapper.toDto(sampleItem)).thenReturn(sampleItemResponse);

        ItemResponse result = itemService.getItemById(DEFAULT_ITEM_ID);

        assertThat(result).isEqualTo(sampleItemResponse);
        Mockito.verify(itemDAO).findById(DEFAULT_ITEM_ID);
        Mockito.verify(itemMapper).toDto(sampleItem);
    }

    @Test
    void getItemById_shouldThrowItemNotFoundException_whenItemDoesNotExist() {
        Mockito.when(itemDAO.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.getItemById(NON_EXISTENT_ID))
                .isInstanceOf(ItemNotFoundException.class)
                .hasMessageContaining("Item not found with id: " + NON_EXISTENT_ID);

        Mockito.verify(itemDAO).findById(NON_EXISTENT_ID);
        Mockito.verifyNoInteractions(itemMapper);
    }

    @Test
    void getItemById_shouldThrowIllegalArgumentException_whenIdIsNull() {
        assertThatThrownBy(() -> itemService.getItemById(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Item's id cannot be null");

        Mockito.verifyNoInteractions(itemDAO, itemMapper);
    }

    @Test
    void getItemById_shouldThrowIllegalArgumentException_whenIdIsZeroOrNegative() {
        assertThatThrownBy(() -> itemService.getItemById(INVALID_ID_ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Item id must be positive");

        assertThatThrownBy(() -> itemService.getItemById(INVALID_ID_NEGATIVE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Item id must be positive");

        Mockito.verifyNoInteractions(itemDAO, itemMapper);
    }
}