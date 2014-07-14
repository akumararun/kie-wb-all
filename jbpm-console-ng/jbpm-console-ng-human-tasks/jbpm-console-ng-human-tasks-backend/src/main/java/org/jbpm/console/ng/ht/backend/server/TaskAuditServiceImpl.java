/*
 * Copyright 2014 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.console.ng.ht.backend.server;


import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.jboss.errai.bus.server.annotations.Service;
import org.jbpm.console.ng.ga.model.QueryFilter;
import org.jbpm.console.ng.ht.model.TaskEventSummary;
import org.jbpm.console.ng.ht.service.TaskAuditService;
import org.jbpm.services.task.query.QueryFilterImpl;
import org.uberfire.paging.PageResponse;

/**
 *
 * @author salaboy
 */
@Service
@ApplicationScoped
public class TaskAuditServiceImpl implements TaskAuditService {

  @Inject
  private org.jbpm.services.task.audit.service.TaskAuditService taskAuditService;


  public TaskAuditServiceImpl() {
  }
 

  @Override
  public PageResponse<TaskEventSummary> getData(QueryFilter filter) {
    PageResponse<TaskEventSummary> response = new PageResponse<TaskEventSummary>();
    
    Long taskId = null;
    if (filter.getParams() != null) {
      taskId = (Long) filter.getParams().get("taskId");

    }
   
    
    org.kie.internal.task.api.QueryFilter qf = new QueryFilterImpl(filter.getOffset(), filter.getCount() + 1, 
                                                                    filter.getOrderBy(), filter.isAscending());
    List<TaskEventSummary> taskEventSummaries = TaskEventSummaryHelper.adaptCollection(taskAuditService.getAllTaskEvents( taskId, qf));

    response.setStartRowIndex(filter.getOffset());
    response.setTotalRowSize(taskEventSummaries.size() - 1);
    if(taskEventSummaries.size() > filter.getCount()){
      response.setTotalRowSizeExact(false);
    }else{
      response.setTotalRowSizeExact(true);
    }

    if (!taskEventSummaries.isEmpty() && taskEventSummaries.size() > (filter.getCount() + filter.getOffset())) {
      response.setPageRowList(new ArrayList<TaskEventSummary>(taskEventSummaries.subList(filter.getOffset(), filter.getOffset() + filter.getCount())));
      response.setLastPage(false);

    } else {
      response.setPageRowList(new ArrayList<TaskEventSummary>(taskEventSummaries));
      response.setLastPage(true);

    }
    return response;
  }

  @Override
  public TaskEventSummary getItem(Object id) {
        return null;
  }

}
