package cz.pwf.whisperer.model.mapper;

import cz.pwf.whisperer.pwf_ods_whisperers_api_client.model.ODSWorkOrderTaskResponse;
import cz.pwf.whisperer.model.WorkOrderTask;
import org.mapstruct.Mapper;

/**
 * Třída umožňující vzájemné mapování modelových tříd, vztahujících se k ÚPP.
 */
@Mapper(componentModel = "spring")
public abstract class WorkOrderTaskMapper {

    public abstract WorkOrderTask toWorkOrderTask(ODSWorkOrderTaskResponse source);
}