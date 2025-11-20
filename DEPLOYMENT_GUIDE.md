# Deployment & Testing Guide

## Quick Start

### 1. Start Core Services
```bash
cd mymicroservices

# Start essential services
docker compose up -d redis postgres postgres-ticket \
  user-service ticket-service api-gateway frontend

# Check status
docker compose ps
```

### 2. Access the Application
- **Frontend**: http://localhost:3000
- **API Gateway**: http://localhost:8082
- **User Service**: http://localhost:8081
- **Ticket Service**: http://localhost:8083

### 3. Test Basic Features
1. Register a new account at http://localhost:3000/register
2. Login at http://localhost:3000/login
3. Navigate to Dashboard
4. Test existing features:
   - ✅ Tickets page
   - ✅ Subscriptions page
   - ✅ Profile page

### 4. Test New Integrated Features

#### Live Map (Without Backend Services)
```bash
# Navigate to http://localhost:3000/live-map
```
**Expected Behavior:**
- Page loads successfully
- Shows "Offline" status (red chip)
- Shows "0 buses tracked"
- Displays placeholder message about map preparation
- UI is fully functional, just no data

#### Trip Planner (Without Backend Services)
```bash
# Navigate to http://localhost:3000/trip-planner
```
**Expected Behavior:**
- Page loads successfully
- Shows autocomplete dropdowns (empty)
- Shows "Failed to load stops and routes" toast
- "Find Routes" button is disabled (no stops selected)
- "Find Nearby Stops" button is functional
- UI is fully functional, just no data

## Full Integration Deployment

### Option A: Deploy All Services (Recommended for Testing)

```bash
# Build all services
docker compose build

# Start all services
docker compose up -d

# Wait for services to be healthy (2-3 minutes)
docker compose ps

# Check logs
docker compose logs -f route-service bus-geolocation-service
```

### Option B: Deploy Only Route & Geolocation Services

```bash
# If core services are already running
docker compose up -d route-service bus-geolocation-service

# Check if they're healthy
docker compose ps | grep -E "(route-service|bus-geolocation)"
```

## Testing With Backend Services

### 1. Verify Route Service
```bash
# Check health
curl http://localhost:8085/actuator/health

# Get all routes
curl http://localhost:8082/api/routes

# Get all stops
curl http://localhost:8082/api/stops

# Expected: Sample Paris transit data (3 routes, 7 stops)
```

### 2. Verify Bus Geolocation Service
```bash
# Check health
curl http://localhost:8086/actuator/health

# Get all buses
curl http://localhost:8082/api/buses

# Get active alerts
curl http://localhost:8082/api/alerts/active
```

### 3. Test Trip Planner with Data
1. Navigate to http://localhost:3000/trip-planner
2. **Expected:** Autocomplete dropdowns now populated with stops
3. Select "Châtelet" as origin
4. Select "Nation" as destination
5. Click "Find Routes"
6. **Expected:** Route options displayed with Metro Line 1 or RER A

### 4. Test Live Map with Data
1. Navigate to http://localhost:3000/live-map
2. **Expected:** 
   - Status shows "Live" (green) if WebSocket connects
   - Bus count updates if simulation is running
   - Bus cards appear with real-time data

## Troubleshooting

### Frontend Not Loading
```bash
# Check frontend container
docker compose logs frontend

# Rebuild if needed
docker compose build --no-cache frontend
docker compose up -d frontend

# Clear browser cache (Ctrl+Shift+Delete)
```

### Backend Services Not Starting
```bash
# Check logs
docker compose logs route-service
docker compose logs bus-geolocation-service

# Common issues:
# 1. Port conflicts - check if ports 8085, 8086 are free
# 2. Database not ready - wait for postgres containers to be healthy
# 3. Build errors - check compilation errors in logs
```

### WebSocket Not Connecting
```bash
# Check if bus-geolocation-service is running
docker compose ps bus-geolocation-service

# Check WebSocket endpoint
curl -i -N -H "Connection: Upgrade" \
  -H "Upgrade: websocket" \
  http://localhost:8086/ws

# Check browser console for WebSocket errors
```

### No Data in Trip Planner
```bash
# Verify Route Service is running and has data
curl http://localhost:8082/api/routes
curl http://localhost:8082/api/stops

# If empty, check DataLoader logs
docker compose logs route-service | grep "DataLoader"

# Should see: "Loading sample Paris transit data..."
```

### CORS Issues
```bash
# Check API Gateway logs
docker compose logs api-gateway | grep CORS

# Verify CORS configuration in api-gateway
# Should allow http://localhost:3000
```

## Development Workflow

### Making Frontend Changes
```bash
# 1. Edit files in Frontend/src/
# 2. Rebuild frontend
docker compose build --no-cache frontend

# 3. Restart frontend
docker compose up -d frontend

# 4. Hard refresh browser (Ctrl+Shift+R)
```

### Making Backend Changes
```bash
# 1. Edit files in Route/ or BusGeolocation/
# 2. Rebuild specific service
docker compose build route-service
# or
docker compose build bus-geolocation-service

# 3. Restart service
docker compose up -d route-service
```

### Viewing Logs
```bash
# All services
docker compose logs -f

# Specific service
docker compose logs -f frontend
docker compose logs -f route-service
docker compose logs -f bus-geolocation-service

# Last 100 lines
docker compose logs --tail=100 route-service
```

## Testing Checklist

### ✅ Core Features (No Backend Required)
- [ ] User registration works
- [ ] User login works
- [ ] Dashboard loads
- [ ] Tickets page loads
- [ ] Can purchase tickets
- [ ] Subscriptions page loads
- [ ] Profile page loads
- [ ] Sidebar navigation works
- [ ] Live Map page loads (shows offline)
- [ ] Trip Planner page loads (shows empty)

### ✅ Integrated Features (Requires Backend)
- [ ] Route Service is healthy
- [ ] Bus Geolocation Service is healthy
- [ ] Trip Planner shows stops in dropdowns
- [ ] Can search for routes between stops
- [ ] Route results display correctly
- [ ] Live Map shows "Live" status
- [ ] WebSocket connects successfully
- [ ] Bus locations update in real-time
- [ ] Alerts display correctly
- [ ] Can click on bus to see details

## Performance Tips

### Reduce Build Time
```bash
# Use cached layers when possible
docker compose build frontend

# Only rebuild what changed
docker compose build route-service
```

### Reduce Startup Time
```bash
# Start services in order of dependency
docker compose up -d redis postgres postgres-ticket
sleep 10
docker compose up -d user-service ticket-service
sleep 10
docker compose up -d api-gateway frontend
```

### Monitor Resource Usage
```bash
# Check container stats
docker stats

# Check disk usage
docker system df

# Clean up if needed
docker system prune -a
```

## Common Commands Reference

```bash
# Start all services
docker compose up -d

# Stop all services
docker compose down

# Restart a service
docker compose restart frontend

# View logs
docker compose logs -f service-name

# Check status
docker compose ps

# Rebuild a service
docker compose build service-name

# Remove volumes (clean database)
docker compose down -v

# Remove everything and start fresh
docker compose down -v
docker system prune -a
docker compose up -d
```

## API Endpoints Reference

### Route Service (via API Gateway)
```
GET  /api/routes              - Get all routes
GET  /api/routes/{id}         - Get route by ID
GET  /api/routes/{id}/stops   - Get route with stops
GET  /api/stops               - Get all stops
GET  /api/stops/{id}          - Get stop by ID
GET  /api/stops/nearby        - Get nearby stops
GET  /api/stops/{id}/next-departures - Get next departures
GET  /api/path/optimal        - Calculate optimal path
GET  /api/schedules/route/{id} - Get route schedules
```

### Bus Geolocation Service (via API Gateway)
```
GET  /api/buses               - Get all buses
GET  /api/buses/{id}          - Get bus by ID
GET  /api/buses/{id}/location - Get bus location
GET  /api/locations/bus/{id}  - Get location history
GET  /api/buses/route         - Get buses on route
GET  /api/alerts/active       - Get active alerts
GET  /api/alerts/bus/{id}     - Get bus alerts
GET  /api/alerts/route/{id}   - Get route alerts
```

### WebSocket (Direct to Geolocation Service)
```
WS   ws://localhost:8086/ws   - WebSocket connection
SUB  /topic/locations         - All bus locations
SUB  /topic/locations/{busId} - Specific bus location
SUB  /topic/alerts            - System alerts
```

## Success Criteria

### Minimal Success (Core Features)
- ✅ Frontend loads and is responsive
- ✅ User can register and login
- ✅ Can navigate between pages
- ✅ Can purchase tickets
- ✅ New pages (Live Map, Trip Planner) load without errors

### Full Success (Integrated Features)
- ✅ All services running and healthy
- ✅ Trip Planner shows real stops and routes
- ✅ Can calculate routes between stops
- ✅ Live Map connects via WebSocket
- ✅ Real-time bus tracking works
- ✅ Alerts display correctly
- ✅ No console errors
- ✅ Responsive design works on mobile

## Support

### Check Documentation
- `INTEGRATION_SUMMARY.md` - Feature overview
- `TROUBLESHOOTING.md` - Common issues
- `README.md` - Project overview

### Debug Steps
1. Check container status: `docker compose ps`
2. Check logs: `docker compose logs service-name`
3. Check browser console (F12)
4. Check network tab for failed requests
5. Verify API endpoints with curl
6. Check database connections
7. Verify environment variables

### Clean Slate
If everything is broken, start fresh:
```bash
# Stop everything
docker compose down -v

# Remove all images
docker rmi $(docker images -q mymicroservices-*)

# Rebuild everything
docker compose build --no-cache

# Start services
docker compose up -d

# Wait for health checks
watch docker compose ps
```

## Next Steps

After successful deployment:
1. Add more sample data (more routes, stops, buses)
2. Implement GPS simulation for realistic bus movement
3. Enable interactive map (resolve React compatibility)
4. Add route visualization on map
5. Implement arrival time predictions
6. Add user preferences (favorite routes/stops)
7. Implement push notifications
8. Add analytics dashboard

