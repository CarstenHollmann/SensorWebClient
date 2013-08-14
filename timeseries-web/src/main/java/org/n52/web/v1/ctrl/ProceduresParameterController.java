
package org.n52.web.v1.ctrl;

import static org.n52.web.v1.ctrl.RestfulUrls.DEFAULT_PATH;

import org.n52.io.v1.data.ProcedureOutput;
import org.n52.web.ResourceNotFoundException;
import org.n52.web.v1.srv.ParameterService;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping(value = DEFAULT_PATH + "/" + RestfulUrls.COLLECTION_PROCEDURES, produces = {"application/json"})
public class ProceduresParameterController extends ParameterController {

    private ParameterService<ProcedureOutput> procedureParameterService;

    public ModelAndView getCollection(@RequestParam(required=false) MultiValueMap<String, String> query) {
        QueryMap map = QueryMap.createFromQuery(query);
        int offset = map.getOffset();
        int size = map.getSize();
        
        if (map.isExpanded()) {
            Object[] result = procedureParameterService.getExpandedParameters(offset, size);

            // TODO add paging
            
            return new ModelAndView().addObject(result);
        } else {
            Object[] result = procedureParameterService.getCondensedParameters(offset, size);

            // TODO add paging
            
            return new ModelAndView().addObject(result);
        }
    }

    public ModelAndView getItem(@PathVariable("item") String procedureId, @RequestParam(required=false) MultiValueMap<String, String> query) {
        QueryMap map = QueryMap.createFromQuery(query);

        // TODO check parameters and throw BAD_REQUEST if invalid

        ProcedureOutput procedure = procedureParameterService.getParameter(procedureId);

        if (procedure == null) {
            throw new ResourceNotFoundException("Found no procedure with given id.");
        }

        return new ModelAndView().addObject(procedure);
    }

    public ParameterService<ProcedureOutput> getProcedureParameterService() {
        return procedureParameterService;
    }

    public void setProcedureParameterService(ParameterService<ProcedureOutput> procedureParameterService) {
        this.procedureParameterService = procedureParameterService;
    }

}