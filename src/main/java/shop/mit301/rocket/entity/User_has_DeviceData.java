    package shop.mit301.rocket.entity;

    import jakarta.persistence.*;
    import lombok.AllArgsConstructor;
    import lombok.Data;
    import lombok.NoArgsConstructor;

    @Entity
    @Table(name = "user_has_devicedata")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class User_has_DeviceData {

        @EmbeddedId
        private User_has_DeviceDataId id;

        @ManyToOne
        @MapsId("userId")
        @JoinColumn(name = "user_id")
        private User user;

        @ManyToOne
        @MapsId("deviceDataId")
        @JoinColumn(name = "devicedata_id")
        private DeviceData deviceData;

    }
