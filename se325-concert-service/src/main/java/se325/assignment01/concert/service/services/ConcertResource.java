package se325.assignment01.concert.service.services;

import se325.assignment01.concert.common.dto.*;
import se325.assignment01.concert.service.domain.*;
import se325.assignment01.concert.service.mapper.*;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Web service resource implementation for the Concert application. An instance
 * of this class handles all HTTP requests for the Concert Web service.
 */
@Path("/concert-service")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ConcertResource {

    private static final List<Subscription> subscriptions = new ArrayList<>();

    /**
     * Get a particular concert represented by a ConcertDTO.
     * @param id The unique identifier of the concert to get.
     * @return A Response containing a ConcertDTO that represents
     * the concert that we need to get.
     */
    @GET
    @Path("/concerts/{id}")
    public Response getSingleConcert(@PathParam("id")long id) {
        EntityManager em = PersistenceManager.instance().createEntityManager();
        em.getTransaction().begin();

        List<Concert> concertList = em.createQuery("SELECT DISTINCT concert " +
                "FROM Concert concert " +
                "LEFT OUTER JOIN FETCH concert.dates " +
                "LEFT OUTER JOIN FETCH concert.performers " +
                "WHERE concert.id = :id", Concert.class)
                .setParameter("id", id)
                .getResultList();

        em.getTransaction().commit();
        em.close();

        if (concertList == null || concertList.isEmpty()) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        Concert concert = concertList.get(0);

        return Response.ok(ConcertMapper.toDto(concert)).build();
    }

    /**
     * Get all the concerts that are in the database for this Concert
     * Application.
     * @return A list of ConcertDTOs that represent all the concerts
     * in the application.
     */
    @GET
    @Path("/concerts/")
    public Response getAllConcerts() {
        EntityManager em = PersistenceManager.instance().createEntityManager();
        em.getTransaction().begin();

        List<Concert> concertList = em.createQuery("SELECT DISTINCT concert " +
                "FROM Concert concert " +
                "LEFT OUTER JOIN FETCH concert.dates " +
                "LEFT OUTER JOIN FETCH concert.performers", Concert.class)
                .getResultList();

        em.getTransaction().commit();
        em.close();

        // convert to DTO
        List<ConcertDTO> concertDTOList = new ArrayList<>();
        for (Concert concert: concertList) {
            concertDTOList.add(ConcertMapper.toDto(concert));
        }

        return Response.ok(concertDTOList).build();
    }

    /**
     * Get the summaries of all concerts that are in the application.
     * @return Summaries of all concerts.
     */
    @GET
    @Path("/concerts/summaries")
    public Response getAllConcertSummaries() {
        EntityManager em = PersistenceManager.instance().createEntityManager();
        em.getTransaction().begin();

        List<Concert> concertList = em.createQuery("SELECT concert " +
                "FROM Concert concert", Concert.class).getResultList();

        em.getTransaction().commit();
        em.close();

        List<ConcertSummaryDTO> concertSummaryDTOList = new ArrayList<>();

        for (Concert concert: concertList) {
            concertSummaryDTOList.add(ConcertSummaryMapper.toDto(concert));
        }

        return Response.ok(concertSummaryDTOList).build();
    }

    /**
     * Get a particular performer from the database.
     * @param id The unique identifer of the performer to get.
     * @return The performer as a PerformerDTO.
     */
    @GET
    @Path("/performers/{id}")
    public Response getSinglePerformer(@PathParam("id")long id) {
        EntityManager em = PersistenceManager.instance().createEntityManager();
        em.getTransaction().begin();

        Performer performer = em.find(Performer.class, id);

        em.getTransaction().commit();
        em.close();

        if (performer == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        return Response.ok(PerformerMapper.toDto(performer)).build();
    }

    /**
     * Get all performers in the application.
     * @return A list of all performers as a List<PerformerDTO>.
     */
    @GET
    @Path("/performers")
    public Response getAllPerformers() {
        EntityManager em = PersistenceManager.instance().createEntityManager();
        em.getTransaction().begin();

        List<Performer> performerList = em.createQuery("SELECT performer " +
                "FROM Performer performer", Performer.class).getResultList();

        em.getTransaction().commit();
        em.close();

        // convert to DTO
        List<PerformerDTO> performerDTOList = new ArrayList<>();
        for (Performer performer: performerList) {
            performerDTOList.add(PerformerMapper.toDto(performer));
        }

        return Response.ok(performerDTOList).build();
    }

    /**
     * Verify the username and password of a user and log them in by giving
     * them an authentication token as a cookie.
     * @param userDTO User information containing username and password.
     * @return Cookie with authentication token.
     */
    @POST
    @Path("/login")
    public Response login(UserDTO userDTO) {
        EntityManager em = PersistenceManager.instance().createEntityManager();
        em.getTransaction().begin();

        List<User> userList = em.createQuery("SELECT user FROM User user " +
                "WHERE user.username = :username AND user.password = :password", User.class)
                .setParameter("username", userDTO.getUsername())
                .setParameter("password", userDTO.getPassword())
                .getResultList();

        if (userList == null || userList.isEmpty()) {
            em.getTransaction().rollback();
            em.close();
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }

        // valid login, create cookie
        String authToken = UUID.randomUUID().toString();
        NewCookie newCookie = new NewCookie("auth", authToken);

        // add cookie to database and connect with user
        em.persist(new Authentication(authToken, userList.get(0)));
        em.getTransaction().commit();
        em.close();

        // return cookie to client
        return Response.ok().cookie(newCookie).build();
    }

    /**
     * Make a booking for a particular user.
     * @param cookie The cookie that authenticates the user.
     * @param bookingRequestDTO Contains booking information for the
     *                          booking to make.
     * @return A URI link to get the new booking that was made.
     */
    @POST
    @Path("/bookings")
    public Response book(@CookieParam("auth") Cookie cookie, BookingRequestDTO bookingRequestDTO) {
        if (cookie == null) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }

        String authToken = cookie.getValue();

        EntityManager em = PersistenceManager.instance().createEntityManager();
        em.getTransaction().begin();

        // authenticate user
        Authentication authentication = em.find(Authentication.class, authToken);

        if (authentication == null) {
            em.getTransaction().rollback();
            em.close();
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }

        User user = authentication.getUser();

        Concert concert = findConcertOnDate(em, bookingRequestDTO.getConcertId(),
                bookingRequestDTO.getDate());

        // Make sure that the user does not change during the time of making
        // the booking for them. The user has a list of bookings that could
        // change, and we want to make sure that other concurrent accesses
        // to the user (e.g. concurrently adding another booking), doesn't
        // cause data integrity violations.
        em.lock(user, LockModeType.OPTIMISTIC_FORCE_INCREMENT);

        Booking booking = makeBooking(bookingRequestDTO, em, concert, user);
        em.persist(booking);

        try {
            em.getTransaction().commit();
        } catch (Exception e){
            // in the case of any exception (especially an optimistic lock
            // exception), we need to roll back our changes.
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }

        // our bookings have changed so update our subscribers accordingly.
        updateConcertSubscriptions(concert);

        try {
            return Response.created(new URI("concert-service/bookings/" + booking.getId())).build();
        } catch (URISyntaxException e) {
            em.getTransaction().rollback();
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get a particular booking using its unique identifier.
     * @param cookie Authentication for user.
     * @param id identifier for booking.
     * @return the booking.
     */
    @GET
    @Path("/bookings/{id}")
    public Response getSingleBooking(@CookieParam("auth") Cookie cookie, @PathParam("id")long id) {
        if (cookie == null) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }

        EntityManager em = PersistenceManager.instance().createEntityManager();
        em.getTransaction().begin();

        List<Booking> bookingList = em.createQuery("SELECT DISTINCT booking " +
                "FROM Booking booking " +
                "LEFT OUTER JOIN FETCH booking.seats " +
                "LEFT OUTER JOIN FETCH booking.user " +
                "LEFT OUTER JOIN FETCH booking.concert " +
                "WHERE booking.id = :id", Booking.class)
                .setParameter("id", id).getResultList();

        if (bookingList == null || bookingList.isEmpty()) {
            em.getTransaction().rollback();
            em.close();
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        Booking booking = bookingList.get(0);

        User user = booking.getUser();
        Authentication authentication = em.find(Authentication.class, cookie.getValue());

        if (!(user.getId() == authentication.getUser().getId())) {
            em.getTransaction().rollback();
            em.close();
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }

        em.getTransaction().commit();
        em.close();

        return Response.ok(BookingMapper.toDto(booking)).build();
    }

    /**
     * Get all the bookings that a user has made.
     * @param cookie Authentication for user.
     * @return A list of bookings that the user has made.
     */
    @GET
    @Path("/bookings")
    public Response getAllBookingsForUser(@CookieParam("auth")Cookie cookie) {
        if (cookie == null) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }

        EntityManager em = PersistenceManager.instance().createEntityManager();
        em.getTransaction().begin();

        // Eager fetch as we need all the information to form the list
        // of bookings that the user has
        List<Authentication> authenticationList = em.createQuery("SELECT DISTINCT auth " +
                "FROM Authentication auth " +
                "LEFT OUTER JOIN FETCH auth.user user " +
                "LEFT OUTER JOIN FETCH user.bookings bookings " +
                "LEFT OUTER JOIN FETCH bookings.seats " +
                "LEFT OUTER JOIN FETCH bookings.concert " +
                "WHERE auth.authToken = :authToken", Authentication.class)
                .setParameter("authToken", cookie.getValue())
                .getResultList();

        // authenticate user
        if (authenticationList == null || authenticationList.isEmpty()) {
            em.getTransaction().rollback();
            em.close();
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }

        em.getTransaction().commit();
        em.close();

        Authentication authentication = authenticationList.get(0);
        User user = authentication.getUser();
        Set<Booking> bookings = user.getBookings();

        // convert to DTO
        List<BookingDTO> bookingDTOS = new ArrayList<>();

        for (Booking booking: bookings) {
            bookingDTOS.add(BookingMapper.toDto(booking));
        }

        return Response.ok(bookingDTOS).build();
    }

    /**
     * Get a list of seats that are booked for a particular date.
     * @param dateStr The date that the seats are available for.
     * @param status The booked status of the seat. Can be Booked, Unbooked, Any.
     * @return A list of seats depending on the status and date.
     */
    @GET
    @Path("/seats/{date}")
    public Response getSeats(@PathParam("date")String dateStr, @QueryParam("status")String status) {
        EntityManager em = PersistenceManager.instance().createEntityManager();

        LocalDateTime date = LocalDateTime.parse(dateStr);

        List<SeatDTO> seatDTOList = new ArrayList<>();
        List<Seat> seats;

        em.getTransaction().begin();

        // Gets a list of seats depending on the status criteria.
        // We could get a list of all seats and check our status criteria
        // in the memory, but it is more efficient to just do it in the
        // database as we then don't need to send the extra information to
        // our application.
        if (status.equals("Booked")) {
            seats = em.createQuery("SELECT seat " +
                    "FROM Seat seat " +
                    "WHERE seat.date = :date " +
                    "AND seat.isBooked = true", Seat.class)
                    .setParameter("date", date).getResultList();
        } else if (status.equals("Unbooked")) {
            seats = em.createQuery("SELECT seat " +
                    "FROM Seat seat " +
                    "WHERE seat.date = :date " +
                    "AND seat.isBooked = false", Seat.class)
                    .setParameter("date", date).getResultList();
        } else {
            seats = em.createQuery("SELECT seat " +
                    "FROM Seat seat " +
                    "WHERE seat.date = :date", Seat.class)
                    .setParameter("date", date).getResultList();
        }

        em.getTransaction().commit();
        em.close();

        for (Seat seat: seats) {
            seatDTOList.add(SeatMapper.toDto(seat));
        }

        return Response.ok(seatDTOList).build();
    }

    /**
     * Adds a subscriber to observe changes in our bookings.
     * @param cookie The authentication token of the user. A user must be
     *               logged in to subscribe.
     * @param subscriber The subscriber response instance of the client. This
     *                   should be resumed to sent a message back to the client.
     * @param concertInfoSubscriptionDTO Information regarding what to subscribe to.
     */
    @POST
    @Path("/subscribe/concertInfo")
    public void subscribeToConcert(@CookieParam("auth") Cookie cookie, @Suspended AsyncResponse subscriber,
                                   ConcertInfoSubscriptionDTO concertInfoSubscriptionDTO) {
        if (cookie == null) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }

        EntityManager em = PersistenceManager.instance().createEntityManager();
        em.getTransaction().begin();

        // authenticate user
        Authentication authentication = em.find(Authentication.class, cookie.getValue());

        if (authentication == null) {
            em.getTransaction().rollback();
            em.close();
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }

        // find if the concert exists
        Concert concert = findConcertOnDate(em, concertInfoSubscriptionDTO.getConcertId(),
                concertInfoSubscriptionDTO.getDate());

        Subscription subscription = new Subscription(concert, subscriber, concertInfoSubscriptionDTO);

        synchronized (subscriptions){
            subscriptions.add(subscription);
        }

        // make sure that if the subscription already meets its criteria,
        // the client is notified immediately.
        updateConcertSubscription(em, concert, subscription);

        em.getTransaction().commit();
        em.close();
    }

    /**
     * Updates a single subscription if it has met its criteria.
     * @param em Entity manager to query using.
     * @param concert Concert of subscription.
     * @param subscription Subscription information.
     * @return Whether an update has happened.
     */
    private boolean updateConcertSubscription(EntityManager em, Concert concert, Subscription subscription) {
        if (subscription.getConcert().getId() == concert.getId()) {
            // get all the seats because we need to process them anyway
            // to get the count of all seats, and the number of seats in
            // a concert is limited.
            List<Seat> allSeats = em.createQuery("SELECT seat FROM Seat seat " +
                    "WHERE seat.date = :concertDate", Seat.class)
                    .setParameter("concertDate", subscription.getSubscriptionInfo().getDate())
                    .getResultList();

            // calculate booked percentage
            int bookedSeats = 0;
            for (Seat seat: allSeats) {
                if (seat.isBooked()) {
                    bookedSeats++;
                }
            }

            if ((bookedSeats*100/allSeats.size()) >= subscription.getSubscriptionInfo().getPercentageBooked()) {
                ConcertInfoNotificationDTO notificationDTO = new ConcertInfoNotificationDTO(allSeats.size() - bookedSeats);
                subscription.getSubscriber().resume(notificationDTO);
                return true;
            }
        }

        return false;
    }

    /**
     * Updates all subscribers of this resource if their criteria has been met.
     * @param concert The concert that has changed.
     */
    private void updateConcertSubscriptions(Concert concert) {
        EntityManager em = PersistenceManager.instance().createEntityManager();
        em.getTransaction().begin();

        List<Subscription> toRemove = new ArrayList<>();

        synchronized (subscriptions) {
            for (Subscription subscription: subscriptions) {
                if (updateConcertSubscription(em, concert, subscription)) {
                        toRemove.add(subscription);
                }
            }

            // a subscription should only last for one notification.
            subscriptions.removeAll(toRemove);
        }

        em.getTransaction().commit();
        em.close();
    }

    /**
     * Helper class to store subscription information.
     */
    private static class Subscription {
        private final Concert concert;
        private final AsyncResponse subscriber;
        private final ConcertInfoSubscriptionDTO subscriptionInfo;

        private Subscription(Concert concert, AsyncResponse subscriber, ConcertInfoSubscriptionDTO subscriptionInfo) {
            this.concert = concert;
            this.subscriber = subscriber;
            this.subscriptionInfo = subscriptionInfo;
        }

        public Concert getConcert() {
            return concert;
        }

        public AsyncResponse getSubscriber() {
            return subscriber;
        }

        public ConcertInfoSubscriptionDTO getSubscriptionInfo() {
            return subscriptionInfo;
        }
    }

    /**
     * Helper class to make a booking.
     * @param bookingRequestDTO Information about the booking to make.
     * @param em Entity Manager to query our database with.
     * @param concert The concert to book.
     * @param user The user to book for.
     * @return The created booking.
     */
    private Booking makeBooking(BookingRequestDTO bookingRequestDTO,
                                EntityManager em, Concert concert,
                                User user) {

        LocalDateTime date = bookingRequestDTO.getDate();
        Set<Seat> seatsToBook = new HashSet<>();

        List<Seat> foundSeatList = em.createQuery("SELECT seat " +
                "FROM Seat seat " +
                "WHERE seat.label in :seatLabels " +
                "AND seat.date = :date", Seat.class)
                .setParameter("seatLabels", bookingRequestDTO.getSeatLabels())
                .setParameter("date", date)
                // Make sure that the seat does not become booked while we are
                // attempting to book it to prevent seats from being concurrently
                // booked.
                .setLockMode(LockModeType.OPTIMISTIC)
                .getResultList();

        if (foundSeatList == null ||
                foundSeatList.size() != bookingRequestDTO.getSeatLabels().size()) {
            em.getTransaction().rollback();
            em.close();
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        for (Seat seat: foundSeatList) {
            if (seat.isBooked()) {
                em.getTransaction().rollback();
                em.close();
                throw new WebApplicationException(Response.Status.FORBIDDEN);
            }

            seatsToBook.add(seat);
            // update the flag in the seat so that it can't be booked later on.
            seat.setBooked(true);
        }

        return new Booking(concert, user, bookingRequestDTO.getDate(), seatsToBook);

    }

    /**
     * Helper method to find a concert that must be on a given date.
     * @param em Entity Manager to query with.
     * @param concertId Identifier of concert to find.
     * @param date Date that concert must be on.
     * @return Found concert.
     */
    private Concert findConcertOnDate(EntityManager em, long concertId, LocalDateTime date) {
        Concert concert = em.find(Concert.class, concertId);

        if (concert == null || !concert.getDates().contains(date)) {
            em.getTransaction().rollback();
            em.close();
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        return concert;
    }

}
