package com.example.wzpn.web.rest;

import com.example.wzpn.Application;
import com.example.wzpn.domain.Liga;
import com.example.wzpn.repository.LigaRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.hamcrest.Matchers.hasItem;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the LigaResource REST controller.
 *
 * @see LigaResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest
public class LigaResourceTest {

    private static final String DEFAULT_NAZWA = "SAMPLE_TEXT";
    private static final String UPDATED_NAZWA = "UPDATED_TEXT";
    private static final String DEFAULT_OPIS = "SAMPLE_TEXT";
    private static final String UPDATED_OPIS = "UPDATED_TEXT";

    @Inject
    private LigaRepository ligaRepository;

    private MockMvc restLigaMockMvc;

    private Liga liga;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        LigaResource ligaResource = new LigaResource();
        ReflectionTestUtils.setField(ligaResource, "ligaRepository", ligaRepository);
        this.restLigaMockMvc = MockMvcBuilders.standaloneSetup(ligaResource).build();
    }

    @Before
    public void initTest() {
        liga = new Liga();
        liga.setNazwa(DEFAULT_NAZWA);
        liga.setOpis(DEFAULT_OPIS);
    }

    @Test
    @Transactional
    public void createLiga() throws Exception {
        int databaseSizeBeforeCreate = ligaRepository.findAll().size();

        // Create the Liga
        restLigaMockMvc.perform(post("/api/ligas")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(liga)))
                .andExpect(status().isCreated());

        // Validate the Liga in the database
        List<Liga> ligas = ligaRepository.findAll();
        assertThat(ligas).hasSize(databaseSizeBeforeCreate + 1);
        Liga testLiga = ligas.get(ligas.size() - 1);
        assertThat(testLiga.getNazwa()).isEqualTo(DEFAULT_NAZWA);
        assertThat(testLiga.getOpis()).isEqualTo(DEFAULT_OPIS);
    }

    @Test
    @Transactional
    public void getAllLigas() throws Exception {
        // Initialize the database
        ligaRepository.saveAndFlush(liga);

        // Get all the ligas
        restLigaMockMvc.perform(get("/api/ligas"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(hasItem(liga.getId().intValue())))
                .andExpect(jsonPath("$.[*].nazwa").value(hasItem(DEFAULT_NAZWA.toString())))
                .andExpect(jsonPath("$.[*].opis").value(hasItem(DEFAULT_OPIS.toString())));
    }

    @Test
    @Transactional
    public void getLiga() throws Exception {
        // Initialize the database
        ligaRepository.saveAndFlush(liga);

        // Get the liga
        restLigaMockMvc.perform(get("/api/ligas/{id}", liga.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(liga.getId().intValue()))
            .andExpect(jsonPath("$.nazwa").value(DEFAULT_NAZWA.toString()))
            .andExpect(jsonPath("$.opis").value(DEFAULT_OPIS.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingLiga() throws Exception {
        // Get the liga
        restLigaMockMvc.perform(get("/api/ligas/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateLiga() throws Exception {
        // Initialize the database
        ligaRepository.saveAndFlush(liga);
		
		int databaseSizeBeforeUpdate = ligaRepository.findAll().size();

        // Update the liga
        liga.setNazwa(UPDATED_NAZWA);
        liga.setOpis(UPDATED_OPIS);
        restLigaMockMvc.perform(put("/api/ligas")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(liga)))
                .andExpect(status().isOk());

        // Validate the Liga in the database
        List<Liga> ligas = ligaRepository.findAll();
        assertThat(ligas).hasSize(databaseSizeBeforeUpdate);
        Liga testLiga = ligas.get(ligas.size() - 1);
        assertThat(testLiga.getNazwa()).isEqualTo(UPDATED_NAZWA);
        assertThat(testLiga.getOpis()).isEqualTo(UPDATED_OPIS);
    }

    @Test
    @Transactional
    public void deleteLiga() throws Exception {
        // Initialize the database
        ligaRepository.saveAndFlush(liga);
		
		int databaseSizeBeforeDelete = ligaRepository.findAll().size();

        // Get the liga
        restLigaMockMvc.perform(delete("/api/ligas/{id}", liga.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate the database is empty
        List<Liga> ligas = ligaRepository.findAll();
        assertThat(ligas).hasSize(databaseSizeBeforeDelete - 1);
    }
}
