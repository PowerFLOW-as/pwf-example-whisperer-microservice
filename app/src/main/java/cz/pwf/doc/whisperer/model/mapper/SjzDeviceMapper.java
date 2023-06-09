package cz.pwf.whisperer.model.mapper;

import cz.pwf.whisperer.pwf_ods_whisperers_api_client.model.ODSSjzDeviceResponse;
import cz.pwf.whisperer.model.SjzDevice;
import org.mapstruct.Mapper;

/**
 * Třída umožňující vzájemné mapování modelových tříd, vztahujících se k SJZ zařízení.
 */
@Mapper(componentModel = "spring")
public abstract class SjzDeviceMapper {

    public abstract SjzDevice toSjzDevice(ODSSjzDeviceResponse source);
}