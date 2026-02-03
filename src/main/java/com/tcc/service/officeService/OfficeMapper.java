package com.tcc.service.officeService;

import com.tcc.dtos.OfficeDto;
import com.tcc.entity.Employee;
import com.tcc.entity.Office;
import com.tcc.entity.OfficeCache;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.stream.Collectors;

@Component
public class OfficeMapper {
    public Office dtoToOffice(OfficeDto dto){
        Office office = new Office();
        office.setOfficeName(dto.officeName());
        office.setEmail(dto.email());
        office.setEmployees(Collections.emptyList());
        return office;
    }

    public OfficeDto officeToDto(Office office){
        return new OfficeDto(office.getOfficeName(),office.getEmail(),office.getPhone());
    }



    public OfficeCache officeToCache(Office office){
        OfficeCache cache = new OfficeCache();
        cache.setOfficeId(office.getOfficeId());
        cache.setEmail(office.getEmail());
        cache.setPhone(office.getPhone());
        cache.setEmployees(office.getEmployees() != null ?
                office.getEmployees().stream()
                        .map(Employee::getEmployeeId).collect(Collectors.toList())
                :Collections.emptyList());
        return cache;
    }
    public OfficeDto cacheToOfficeDto(OfficeCache cache){
        return new OfficeDto(cache.getOfficeName(), cache.getEmail(), cache.getPhone());
    }

}
