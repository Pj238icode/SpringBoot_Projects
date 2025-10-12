package com.boot.backend.ContactManager.Responses;

import com.boot.backend.ContactManager.dtos.ContactDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactResponse {
    private List<ContactDto> contacts;
    private int currentPage;
    private int totalPages;
    private long totalItems;
    private int pageSize;
}
