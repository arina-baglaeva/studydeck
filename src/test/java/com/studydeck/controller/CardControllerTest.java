package com.studydeck.controller;

import com.studydeck.exception.NotFoundException;
import com.studydeck.model.Card;
import com.studydeck.model.Deck;
import com.studydeck.service.CardService;
import com.studydeck.service.DeckService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(CardController.class)
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DeckService deckService;

    @MockBean
    private CardService cardService;

    @Test
    void newCardFormIsShown() throws Exception {
        when(deckService.getById(1L)).thenReturn(new Deck("Java", null));

        mockMvc.perform(get("/decks/1/cards/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("card-form"))
                .andExpect(model().attributeExists("cardForm"))
                .andExpect(content().string(containsString("Новая карточка")));
    }

    @Test
    void newCardFormForMissingDeckReturns404() throws Exception {
        when(deckService.getById(999L)).thenThrow(new NotFoundException("Колода не найдена"));

        mockMvc.perform(get("/decks/999/cards/new"))
                .andExpect(status().isNotFound());
    }

    @Test
    void validCardIsAddedAndRedirectsToDeck() throws Exception {
        mockMvc.perform(post("/decks/1/cards")
                        .param("term", "JVM")
                        .param("definition", "виртуальная машина Java"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/decks/1"));

        verify(cardService).addCard(1L, "JVM", "виртуальная машина Java");
    }

    @Test
    void blankTermShowsError() throws Exception {
        when(deckService.getById(1L)).thenReturn(new Deck("Java", null));

        mockMvc.perform(post("/decks/1/cards")
                        .param("term", "  ")
                        .param("definition", "что-то"))
                .andExpect(status().isOk())
                .andExpect(view().name("card-form"))
                .andExpect(model().attributeHasFieldErrors("cardForm", "term"));

        verify(cardService, never()).addCard(any(), any(), any());
    }

    @Test
    void tooLongTermShowsError() throws Exception {
        when(deckService.getById(1L)).thenReturn(new Deck("Java", null));

        mockMvc.perform(post("/decks/1/cards")
                        .param("term", "a".repeat(Card.TERM_MAX_LENGTH + 1))
                        .param("definition", "что-то"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("cardForm", "term"));

        verify(cardService, never()).addCard(any(), any(), any());
    }

    @Test
    void tooLongDefinitionShowsError() throws Exception {
        when(deckService.getById(1L)).thenReturn(new Deck("Java", null));

        mockMvc.perform(post("/decks/1/cards")
                        .param("term", "JVM")
                        .param("definition", "a".repeat(Card.DEFINITION_MAX_LENGTH + 1)))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("cardForm", "definition"));

        verify(cardService, never()).addCard(any(), any(), any());
    }

    @Test
    void editFormIsFilledWithCardText() throws Exception {
        when(deckService.getById(1L)).thenReturn(new Deck("Java", null));
        when(cardService.getCard(1L, 7L)).thenReturn(new Card("JDK", "набор разработчика"));

        mockMvc.perform(get("/decks/1/cards/7/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("card-form"))
                .andExpect(content().string(containsString("Редактирование карточки")))
                .andExpect(content().string(containsString("JDK")))
                .andExpect(content().string(containsString("набор разработчика")));
    }

    @Test
    void editFormForCardFromAnotherDeckReturns404() throws Exception {
        when(deckService.getById(2L)).thenReturn(new Deck("Other", null));
        when(cardService.getCard(2L, 7L)).thenThrow(new NotFoundException("Карточка не найдена"));

        mockMvc.perform(get("/decks/2/cards/7/edit"))
                .andExpect(status().isNotFound());
    }

    @Test
    void validEditUpdatesCardAndRedirects() throws Exception {
        mockMvc.perform(post("/decks/1/cards/7")
                        .param("term", "JDK")
                        .param("definition", "Java Development Kit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/decks/1"));

        verify(cardService).updateCard(1L, 7L, "JDK", "Java Development Kit");
    }

    @Test
    void invalidEditShowsErrorAndDoesNotUpdate() throws Exception {
        when(deckService.getById(1L)).thenReturn(new Deck("Java", null));
        when(cardService.getCard(1L, 7L)).thenReturn(new Card("JDK", "набор разработчика"));

        mockMvc.perform(post("/decks/1/cards/7")
                        .param("term", "JDK")
                        .param("definition", ""))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("cardForm", "definition"));

        verify(cardService, never()).updateCard(any(), any(), any(), any());
    }

    @Test
    void editOfCardFromAnotherDeckReturns404() throws Exception {
        when(cardService.updateCard(2L, 7L, "x", "y")).thenThrow(new NotFoundException("Карточка не найдена"));

        mockMvc.perform(post("/decks/2/cards/7")
                        .param("term", "x")
                        .param("definition", "y"))
                .andExpect(status().isNotFound());
    }

    @Test
    void cardTextIsEscapedInEditForm() throws Exception {
        when(deckService.getById(1L)).thenReturn(new Deck("Java", null));
        when(cardService.getCard(1L, 7L)).thenReturn(new Card("<script>x</script>", "ok"));

        mockMvc.perform(get("/decks/1/cards/7/edit"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("&lt;script&gt;")));
    }

    @Test
    void deleteCardRedirectsToDeck() throws Exception {
        mockMvc.perform(post("/decks/1/cards/7/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/decks/1"));

        verify(cardService).deleteCard(1L, 7L);
    }

    @Test
    void deleteCardFromAnotherDeckReturns404() throws Exception {
        doThrow(new NotFoundException("Карточка не найдена")).when(cardService).deleteCard(2L, 7L);

        mockMvc.perform(post("/decks/2/cards/7/delete"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCardWithGetIsNotAllowed() throws Exception {
        mockMvc.perform(get("/decks/1/cards/7/delete"))
                .andExpect(status().isMethodNotAllowed());

        verify(cardService, never()).deleteCard(any(), any());
    }
}
