package com.buildings.mapper;

import com.buildings.dto.response.bill.BillDTO;
import com.buildings.dto.response.bill.BillDetailDTO;
import com.buildings.entity.BillDetail;
import com.buildings.entity.MaintenanceItem;
import com.buildings.entity.MaintenanceQuotation;
import com.buildings.entity.MeterReading;
import com.buildings.entity.MonthlyBills;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MonthlyBillMapper {

    @Mapping(target = "apartmentId", source = "apartment.id")
    @Mapping(target = "apartmentCode", source = "apartment.code")
    @Mapping(target = "buildingCode", source = "apartment.building.code")
    BillDTO toDto(MonthlyBills entity);

    BillDetailDTO toDetailDto(BillDetail entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "bill", ignore = true)
    @Mapping(target = "description", expression = "java(\"Phí bảo trì [\" + quotation.getMaintenanceRequest().getCode() + \"]: \" + quotation.getTitle())")
    @Mapping(target = "quantity", constant = "1.0")
    @Mapping(target = "unitPrice", source = "totalAmount")
    @Mapping(target = "amount", source = "totalAmount")
    @Mapping(target = "taxRate", constant = "0.0")
    @Mapping(target = "totalLine", source = "totalAmount")
    BillDetail toBillDetail(MaintenanceQuotation quotation);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "bill", ignore = true)
    @Mapping(target = "description", expression = "java(\"Phí bảo trì [\" + requestCode + \"]: \" + item.getName())")
    @Mapping(target = "quantity", expression = "java(item.getQuantity() != null ? item.getQuantity().doubleValue() : 0.0)")
    @Mapping(target = "unitPrice", expression = "java(item.getUnitPrice() != null ? item.getUnitPrice().doubleValue() : 0.0)")
    @Mapping(target = "amount", expression = "java((item.getUnitPrice() != null ? item.getUnitPrice().doubleValue() : 0.0) * (item.getQuantity() != null ? item.getQuantity() : 0))")
    @Mapping(target = "taxRate", constant = "0.0")
    @Mapping(target = "totalLine", expression = "java((item.getUnitPrice() != null ? item.getUnitPrice().doubleValue() : 0.0) * (item.getQuantity() != null ? item.getQuantity() : 0))")
    BillDetail toBillDetail(MaintenanceItem item, String requestCode);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "bill", ignore = true)
    @Mapping(target = "description", expression = "java(\"Phí DV: \" + meterReading.getService().getName())")
    @Mapping(target = "quantity", source = "consumption")
    @Mapping(target = "unitPrice", ignore = true)
    @Mapping(target = "amount", ignore = true)
    @Mapping(target = "taxRate", ignore = true)
    @Mapping(target = "totalLine", ignore = true)
    BillDetail toBillDetailBase(MeterReading meterReading);
}
