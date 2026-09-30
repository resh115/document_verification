(() => {
    const canvas = document.getElementById("globe-canvas");
    if (!canvas || !window.THREE) {
        return;
    }

    const scene = new THREE.Scene();
    const camera = new THREE.PerspectiveCamera(28, 1, 0.1, 100);
    camera.position.z = 7.2;

    const renderer = new THREE.WebGLRenderer({
        canvas,
        antialias: true,
        alpha: true,
        powerPreference: "high-performance"
    });
    renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
    renderer.setClearColor(0x000000, 0);

    const globe = new THREE.Group();
    const radius = 1.2;
    const globeSizePixels = 11 * 96 / 2.54;
    scene.add(globe);

    const lineMaterial = new THREE.LineBasicMaterial({
        color: 0xcbd4dc,
        transparent: true,
        opacity: 0.42
    });
    const glowMaterial = new THREE.LineBasicMaterial({
        color: 0x8edcff,
        transparent: true,
        opacity: 0.12
    });
    const landMaterial = new THREE.LineBasicMaterial({
        color: 0xffffff,
        transparent: true,
        opacity: 0.95
    });

    function addLine(points, material) {
        if (points.length < 2) {
            return;
        }
        const geometry = new THREE.BufferGeometry().setFromPoints(points);
        globe.add(new THREE.Line(geometry, material));
    }

    function globePoint(latitude, longitude, scale = radius) {
        const lat = THREE.MathUtils.degToRad(latitude);
        const lon = THREE.MathUtils.degToRad(longitude);
        return new THREE.Vector3(
            scale * Math.cos(lat) * Math.cos(lon),
            scale * Math.sin(lat),
            scale * Math.cos(lat) * Math.sin(lon)
        );
    }

    function addGrid() {
        for (let latitude = -80; latitude <= 80; latitude += 8) {
            const points = [];
            for (let longitude = -180; longitude <= 180; longitude += 3) {
                points.push(globePoint(latitude, longitude));
            }
            addLine(points, lineMaterial);
            addLine(points.map(point => point.clone().multiplyScalar(1.004)), glowMaterial);
        }
        for (let longitude = -180; longitude < 180; longitude += 8) {
            const points = [];
            for (let latitude = -90; latitude <= 90; latitude += 3) {
                points.push(globePoint(latitude, longitude));
            }
            addLine(points, lineMaterial);
            addLine(points.map(point => point.clone().multiplyScalar(1.004)), glowMaterial);
        }
    }

    function addGeoJsonGeometry(geometry) {
        if (!geometry) {
            return;
        }
        if (geometry.type === "LineString") {
            addLine(geometry.coordinates.map(([longitude, latitude]) => globePoint(latitude, longitude, radius + 0.012)), landMaterial);
        } else if (geometry.type === "MultiLineString") {
            geometry.coordinates.forEach(line => addGeoJsonGeometry({ type: "LineString", coordinates: line }));
        } else if (geometry.type === "Polygon") {
            geometry.coordinates.forEach(line => addGeoJsonGeometry({ type: "LineString", coordinates: line }));
        } else if (geometry.type === "MultiPolygon") {
            geometry.coordinates.forEach(polygon => addGeoJsonGeometry({ type: "Polygon", coordinates: polygon }));
        }
    }

    addGrid();

    const target = { x: -0.15, y: -0.45 };
    const velocity = { x: 0, y: 0 };
    let dragging = false;
    let previousPointer = { x: 0, y: 0 };

    function updateTarget(clientX, clientY) {
        const x = (clientX / window.innerWidth) * 2 - 1;
        const y = (clientY / window.innerHeight) * 2 - 1;
        target.y = THREE.MathUtils.clamp(x * 0.9, -1.05, 1.05);
        target.x = THREE.MathUtils.clamp(y * 0.5, -0.55, 0.55);
    }

    window.addEventListener("pointermove", event => {
        updateTarget(event.clientX, event.clientY);
        if (!dragging) {
            return;
        }
        const dx = event.clientX - previousPointer.x;
        const dy = event.clientY - previousPointer.y;
        target.y += dx * 0.008;
        target.x += dy * 0.005;
        velocity.y = dx * 0.0008;
        velocity.x = dy * 0.0005;
        previousPointer = { x: event.clientX, y: event.clientY };
    });

    canvas.addEventListener("pointerdown", event => {
        dragging = true;
        previousPointer = { x: event.clientX, y: event.clientY };
        canvas.setPointerCapture(event.pointerId);
    });

    canvas.addEventListener("pointerup", event => {
        dragging = false;
        canvas.releasePointerCapture(event.pointerId);
    });

    canvas.addEventListener("pointercancel", () => {
        dragging = false;
    });

    function resize() {
        const width = canvas.clientWidth;
        const height = canvas.clientHeight;
        renderer.setSize(width, height, false);
        camera.aspect = width / height;
        camera.updateProjectionMatrix();
        const projectedBaseSize = radius * height / (Math.tan(THREE.MathUtils.degToRad(14)) * camera.position.z);
        globe.scale.setScalar(globeSizePixels / projectedBaseSize);
    }

    window.addEventListener("resize", resize);
    resize();

    fetch("https://cdn.jsdelivr.net/npm/world-atlas@2/land-110m.json")
        .then(response => response.json())
        .then(world => {
            if (!window.topojson) {
                return;
            }
            addGeoJsonGeometry(topojson.feature(world, world.objects.land).geometry);
        })
        .catch(() => {
            // The latitude/longitude wireframe remains usable if the map data is unavailable.
        });

    function animate() {
        requestAnimationFrame(animate);
        if (!dragging) {
            velocity.x *= 0.94;
            velocity.y *= 0.94;
            target.y += velocity.y;
            target.x += velocity.x;
        }
        globe.rotation.x += (target.x - globe.rotation.x) * 0.035;
        globe.rotation.y += (target.y - globe.rotation.y) * 0.035;
        globe.rotation.x = THREE.MathUtils.clamp(globe.rotation.x, -0.8, 0.8);
        renderer.render(scene, camera);
    }

    animate();
})();
