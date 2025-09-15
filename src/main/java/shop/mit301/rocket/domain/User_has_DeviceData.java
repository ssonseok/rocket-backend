    package shop.mit301.rocket.domain;

    import jakarta.persistence.*;
    import lombok.AllArgsConstructor;
    import lombok.Data;
    import lombok.NoArgsConstructor;

    @Entity
    @Table(name = "user_has_device_data")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class User_has_DeviceData {

        @EmbeddedId
        private User_has_DeviceDataId id;

        @ManyToOne
        @MapsId("user_id")
        @JoinColumn(name = "user_id")
        private User user;

        @ManyToOne
        @MapsId("device_data_id")
        @JoinColumn(name = "device_data_id")
        private DeviceData device_data;

    }
