function getContextPath() {
    var path = window.location.pathname;
    var firstSlash = path.indexOf('/', 1);
    return firstSlash > 0 ? path.substring(0, firstSlash) : "";
}

function refreshNotifications() {
    fetch(getContextPath() + "/notifications", {
        headers: {
            "Accept": "application/xml"
        }
    })
    .then(function(response) {
        if (!response.ok) {
            return null;
        }

        return response.text();
    })
    .then(function(xml) {
        if (!xml) {
            return;
        }

        var parser = new DOMParser();
        var documentXml = parser.parseFromString(
            xml,
            "application/xml"
        );

        var notificationNodes =
            documentXml.querySelectorAll("notification");

        var messages = [];

        for (var i = 0; i < notificationNodes.length; i++) {
            messages.push(notificationNodes[i].textContent);
        }

        var badge =
            document.querySelector("[data-notification-count]");

        if (badge) {
            badge.textContent = messages.length;
            badge.hidden = messages.length === 0;
        }
    })
    .catch(function(error) {
        console.log("Notification refresh skipped.", error);
    });
}

setInterval(refreshNotifications, 30000);

refreshNotifications();