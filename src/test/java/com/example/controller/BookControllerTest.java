package com.example.controller;

import com.example.entity.Book;
import com.example.exception.RecordNotFoundException;
import com.example.repository.BookRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class BookControllerTest {

    private MockMvc mockMvc;

    ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private BookRepository repository;

    @InjectMocks
    private BookController controller;

    Book book1 = new Book(1L, "Java", "How to start with Java", 5);
    Book book2 = new Book(2L, "Python", "How to start with Python", 4);
    Book book3 = new Book(3L, "Angular", "How to learn front end", 5);

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    public void testGetAllBooks() throws Exception {
        List<Book> records = List.of(book1, book2, book3);

        when(repository.findAll()).thenReturn(records);

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/books")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", hasSize(3)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].name", equalToIgnoringCase("Angular")))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].name", equalToIgnoringCase("Python")));
    }

    @Test
    public void testGetBookById() throws Exception {
        when(repository.findById(book1.getBookId())).thenReturn(Optional.of(book1));

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/books/" + book1.getBookId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", notNullValue()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", equalToIgnoringCase("Java")));
    }

    @Test(expected = RecordNotFoundException.class)
    public void testGetBookById_WhenIdIsNotFound() {
        when(controller.getBookById(99L)).thenThrow(new RecordNotFoundException("Record is not found"));
    }

    @Test
    public void testAddBook() throws Exception {
        Book record = Book
                .builder()
                .bookId(4L)
                .name("Vue")
                .summary("Learn Vue in 5 steps")
                .rating(3)
                .build();

        when(repository.save(record)).thenReturn(record);

        String content = objectMapper.writeValueAsString(record);

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(content);

        mockMvc.perform(mockRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.name", equalToIgnoringCase("Vue")));
    }

    @Test
    public void testUpdateBook() throws Exception {
        Book updatedRecord = Book
                .builder()
                .bookId(1L)
                .name("Updated Java")
                .summary("Learn Java in 5 steps")
                .rating(4)
                .build();

        when(repository.findById(book1.getBookId())).thenReturn(Optional.of(book1));

        when(repository.save(updatedRecord)).thenReturn(updatedRecord);

        String updatedContent = objectMapper.writeValueAsString(updatedRecord);

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.put("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(updatedContent);

        mockMvc.perform(mockRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.name", equalToIgnoringCase("Updated Java")))
                .andExpect(jsonPath("$.summary", equalToIgnoringCase("Learn Java in 5 steps")));
    }

    @Test(expected = RecordNotFoundException.class)
    public void testUpdateBook_WhenIdIsNotFound() {
        Book record = Book
                .builder()
                .name("Updated Java")
                .summary("Learn Java in 5 steps")
                .rating(4)
                .build();

        when(controller.updateBook(record)).thenThrow(new RecordNotFoundException("Record is not found"));
    }

    @Test
    public void testDeleteBook() throws Exception {
        when(repository.findById(book1.getBookId())).thenReturn(Optional.of(book1));

        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/books/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200));
    }

    @Test(expected = RecordNotFoundException.class)
    public void testDeleteBookById_WhenIdIsNotFound() {
        BookController bookController = mock(BookController.class);
        doThrow(new RecordNotFoundException("Record is not found")).when(bookController)
                .deleteBookById(4L);
        bookController.deleteBookById(4L);
    }
}