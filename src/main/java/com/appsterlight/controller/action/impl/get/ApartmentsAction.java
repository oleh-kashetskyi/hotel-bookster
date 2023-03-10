package com.appsterlight.controller.action.impl.get;

import com.appsterlight.controller.action.FrontAction;
import com.appsterlight.controller.action.utils.ControllerUtils;
import com.appsterlight.controller.action.utils.DtoUtils;
import com.appsterlight.controller.constants.PagesNames;
import com.appsterlight.controller.context.AppContext;
import com.appsterlight.controller.dto.ApartmentClassDto;
import com.appsterlight.controller.dto.ApartmentDto;
import com.appsterlight.controller.dto.UserDto;
import com.appsterlight.exception.ServiceException;
import com.appsterlight.model.domain.ApartmentClass;
import com.appsterlight.service.ApartmentClassService;
import com.appsterlight.service.ApartmentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.List;

@Slf4j
public class ApartmentsAction extends FrontAction {
    private static final AppContext appContext = AppContext.getAppContext();
    private String chosenStartDate;
    private String chosenEndDate;


    @Override
    public String process(HttpServletRequest req, HttpServletResponse resp) {
        ApartmentService apartmentService = appContext.getApartmentService();
        ApartmentClassService apartmentClassService = appContext.getApartmentClassService();

        final HttpSession session = req.getSession();
        UserDto user = (UserDto) session.getAttribute("loggedUser");
        if (user != null) {
            try {
                List<ApartmentDto> allApartments;
                LocalDate currentDay = LocalDate.now();
                Integer guests = 1;

                String guestsStr = req.getParameter("guests");

                if (guestsStr != null) {
                    guests = Integer.parseInt(guestsStr);
                } else {
                    String guestsFromRequest = (String) req.getAttribute("guests");
                    if (guestsFromRequest != null) {
                        guests = Integer.parseInt(guestsFromRequest);
                    }
                }

                String startDateReq = req.getParameter("startDate");
                String endDateReq = req.getParameter("endDate");

                if (startDateReq != null && endDateReq != null && !startDateReq.equals("")  && !endDateReq.equals("")) {
                    chosenStartDate = startDateReq;
                    chosenEndDate = endDateReq;
                } else {
                    if (chosenStartDate == null || chosenEndDate == null) {
                        chosenStartDate = currentDay.toString();
                        chosenEndDate = chosenStartDate;
                    }
                }

                req.setAttribute("startDate", chosenStartDate);
                req.setAttribute("endDate", chosenEndDate);

                String apartmentClass = req.getParameter("apartmentClass");
                if ((apartmentClass == null) || apartmentClass.equals("0") || apartmentClass.equalsIgnoreCase("All Classes")) {
                    apartmentClass = "0";
                    allApartments = DtoUtils.mapApartmentListToDtoList(
                            apartmentService.getAllFreeApartments(guests, LocalDate.parse(chosenStartDate),
                                    LocalDate.parse(chosenEndDate)));
                } else {
                    allApartments = DtoUtils.mapApartmentListToDtoList(
                            apartmentService.getAllFreeApartmentsByClass(guests, LocalDate.parse(chosenStartDate),
                                    LocalDate.parse(chosenEndDate), Integer.parseInt(apartmentClass)));
                }

                req.setAttribute("guests", guests);
                req.setAttribute("apartments", allApartments);

                List<ApartmentClass> allClasses = apartmentClassService.getAllApartmentClasses();
                List<ApartmentClassDto> allApartmentClasses = DtoUtils.mapApartmentClassListToDtoList(allClasses);
                req.setAttribute("apartmentClasses", allApartmentClasses);
                req.setAttribute("chosenClass", apartmentClass);
            } catch (ServiceException e) {
                log.error("Cant's get all apartments! " + e.getMessage());
                throw new RuntimeException("Cant's get all apartments! " + e.getMessage());
            } catch (Exception e) {
                log.error("Can't parse date of Checkin date, Checkout date or Apartment Class! " + e.getMessage());
                throw new RuntimeException("Cant's get all apartments! " + e.getMessage());
            }
            return ControllerUtils.getApartmentsPageByRole(user.getRole());
        }
        return PagesNames.PAGE_APARTMENTS_GUEST;
    }

}
