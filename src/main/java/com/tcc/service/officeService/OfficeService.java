package com.tcc.service.officeService;

import com.tcc.cache.OfficeCacheRepository;
import com.tcc.components.CursorCodec;
import com.tcc.dtos.OfficeDto;
import com.tcc.dtos.cursors.CursorPageResponse;
import com.tcc.entity.Office;
import com.tcc.entity.OfficeCache;
import com.tcc.exception.ConflictException;
import com.tcc.exception.ResourceNotFoundException;
import com.tcc.pagination.PageDirection;
import com.tcc.persistance.OfficeRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@Transactional
public class OfficeService {
    private static final Logger log = LoggerFactory.getLogger(OfficeService.class);
    private final OfficeMapper mapper;
    private final OfficeRepository repository;
    private final OfficeCacheRepository cacheRepository;
    private final CursorCodec cursorCodec;

    public OfficeService(OfficeMapper mapper, OfficeRepository repository, OfficeCacheRepository cacheRepository, CursorCodec cursorCodec) {
        this.mapper = mapper;
        this.repository = repository;
        this.cacheRepository = cacheRepository;
        this.cursorCodec = cursorCodec;
    }

    public Page<OfficeDto> allOffices(Pageable pageable) {
        return repository.findAll(pageable)
                .map(mapper::officeToDto);
    }

    private static final int maxSize = 50;
    private static final int defaultSize = 20;

    private int normalizeSize(int size){
        if(size <= 0){
            size = defaultSize;
        }

        return Math.min(size,maxSize);
    }


    public CursorPageResponse<OfficeDto> keySetOffices(Integer lastId, int size, PageDirection direction){
        int pageSize = normalizeSize(size);
        Pageable pageable = PageRequest.of(0,pageSize + 1);
        List<Office> offices;

        if(lastId == null){
            offices = repository.findAll(pageable).getContent();
        } else if(direction == PageDirection.NEXT) {
            offices = repository.findByOfficeIdGreaterThanOrderByOfficeIdAsc(lastId,pageable);
        }
        else {
            offices = repository.findByOfficeIdLessThanOrderByOfficeIdDesc(lastId, pageable);
            Collections.reverse(offices);
        }

        boolean hasNext;
        boolean hasPrev;
        if (direction == PageDirection.NEXT){
            hasNext = offices.size() > pageSize;
            hasPrev = lastId != null;
            if (hasNext){
                offices = offices.subList(0,pageSize);
            }
        }
        else{
            hasNext = lastId != null;
            hasPrev = offices.size() > pageSize;
            if (hasPrev){
                offices = offices.subList(1,offices.size());
            }
        }

        String nextCursor = null;
        String prevCursor = null;
        if (!offices.isEmpty()){
            Office lastOffice = offices.getLast();
            nextCursor = cursorCodec.encode(lastOffice.getOfficeId());

            Office firstOffice = offices.getFirst();
            prevCursor = cursorCodec.encode(firstOffice.getOfficeId());
        }

        if (lastId == null){
            hasPrev = false;
            prevCursor = null;
        }
        if (!hasPrev){
            prevCursor= null;
        }
        if(!hasNext){
            nextCursor = null;
        }

        List<OfficeDto> dtos = offices.stream()
                .peek(office -> { cacheRepository.save(mapper.officeToCache(office));})
                .map(mapper::officeToDto)
                .toList();
        return new CursorPageResponse<>(dtos,nextCursor,prevCursor,hasNext,hasPrev);
    }

    public OfficeDto findOfficeById(int id) {
        return cacheRepository.findById(id)
                .map(mapper::cacheToOfficeDto)
                .orElseGet(() -> {
                    Office office = repository.findById(id)
                            .orElseThrow(() -> new ResourceNotFoundException("Office", id));
                    OfficeCache cache = mapper.officeToCache(office);
                    cacheRepository.save(cache);
                    return mapper.officeToDto(office);
                });
    }

    public OfficeDto saveOffice(OfficeDto dto) {
        if (repository.existsByEmail(dto.email())) {
            throw new ConflictException("There is already an office with this email: " + dto.email());
        }
        Office saved = repository.save(mapper.dtoToOffice(dto));

        try {
            cacheRepository.save(mapper.officeToCache(saved));
        } catch (Exception e) {
            log.warn("Redis unavailable, skipping cache for office id={}",saved.getOfficeId());
        }

        return mapper.officeToDto(saved);
    }

    public void deleteById(int id) {
        if(!repository.existsById(id)) {
            throw new ResourceNotFoundException("Office",id);
        }
        repository.deleteById(id);
        cacheRepository.deleteById(id);

    }

    public OfficeDto updateOfficeById(OfficeDto dto, int id) {
        Office oldOffice = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Office", id));
        if (!oldOffice.getEmail().equals(dto.email()) && repository.existsByEmail(dto.email())) {
            throw new ConflictException("There is already an office with this email: " + dto.email());
        }
        oldOffice.setOfficeName(dto.officeName());
        oldOffice.setEmail(dto.email());
        oldOffice.setPhone(dto.phone());
        repository.save(oldOffice);
        cacheRepository.save(mapper.officeToCache(oldOffice));
        return mapper.officeToDto(oldOffice);

    }
}
