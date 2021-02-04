
package subject.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@FeignClient(name="scholarship", url="${api.scholarship.url}")
public interface ScholarshipService {

    @RequestMapping(method= RequestMethod.GET, path="/scholarships")
    public void giveScholarship(@RequestBody Scholarship scholarship);

}