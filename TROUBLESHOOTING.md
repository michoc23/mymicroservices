# Troubleshooting Guide

## Frontend Build Cache Issues

### Issue: New Routes Not Appearing (404 Error)

**Problem Description:**
After adding new routes to the React application (`LiveMapPage` and `TripPlannerPage`), the pages returned 404 errors even though:
- The routes were correctly defined in `App.js`
- The imports were present
- The menu items appeared in the sidebar
- Browser cache was cleared multiple times
- Incognito mode was tested

**Root Cause:**
Docker's build caching mechanism was preventing the new code from being compiled into the JavaScript bundle. Even though the source files were updated, Docker detected that the files hadn't changed by checksum (due to directory renaming operations) and used cached build layers.

### Symptoms
1. Sidebar menu items show the new pages
2. Clicking the menu items results in 404 error
3. Searching the JavaScript bundle for route paths returns no results
4. Browser shows "Route Not Found" page
5. The issue persists even after clearing browser cache

### Solution

**Step 1: Verify Source Code**
```bash
# Check that routes are in App.js
grep "live-map\|trip-planner" Frontend/src/App.js
```

**Step 2: Remove Old Docker Image**
```bash
cd mymicroservices
docker compose down frontend
docker rmi mymicroservices-frontend
```

**Step 3: Force Code Change**
Add a comment or modify a file to ensure Docker detects a change:
```javascript
// In App.js
// Routes updated to include Live Map and Trip Planner
```

**Step 4: Clean Rebuild**
```bash
# Build without cache
docker compose build --no-cache frontend

# Verify new image was created
docker images | grep mymicroservices-frontend
# Should show a recent timestamp (seconds/minutes ago)
```

**Step 5: Deploy and Test**
```bash
# Start the frontend
docker compose up -d frontend

# Test in browser
# Navigate to http://localhost:3000/live-map
# Navigate to http://localhost:3000/trip-planner
```

### Prevention

To avoid this issue in the future:

1. **Always check image timestamps** after building:
   ```bash
   docker images | grep mymicroservices-frontend
   ```
   If the timestamp is old, the cache was used.

2. **Use `--no-cache` for important changes:**
   ```bash
   docker compose build --no-cache frontend
   ```

3. **Verify the bundle contents** after deployment:
   ```bash
   # Check if routes are in the JavaScript bundle
   docker exec urban-transport-frontend sh -c \
     'cat /usr/share/nginx/html/static/js/main.*.js | grep -o "path:\"live-map"'
   ```

4. **Touch files to force cache invalidation:**
   ```bash
   touch Frontend/src/App.js
   docker compose build frontend
   ```

### Related Issues

#### React Version Compatibility
During this troubleshooting, we also discovered dependency conflicts:

**Issue:** `react-leaflet@5.0.0` requires React 19, but the project uses React 18.2.0

**Solution:** Downgrade to compatible versions in `package.json`:
```json
{
  "react-leaflet": "^3.2.5",
  "@stomp/stompjs": "^6.1.2"
}
```

**Build Command:** Updated `Frontend/Dockerfile`:
```dockerfile
RUN npm install --legacy-peer-deps
```

### Verification Checklist

After making changes to frontend routes:

- [ ] Source code updated (`App.js`, page components)
- [ ] Docker image rebuilt (check timestamp)
- [ ] Container restarted
- [ ] Browser cache cleared (Ctrl+Shift+Delete)
- [ ] Test in incognito mode
- [ ] Verify routes in JavaScript bundle
- [ ] Check browser console for errors

### Debug Commands

```bash
# Check running containers
docker compose ps

# Check frontend logs
docker compose logs frontend

# Inspect container filesystem
docker exec urban-transport-frontend ls /usr/share/nginx/html/static/js/

# Check which main.js is being served
docker exec urban-transport-frontend cat /usr/share/nginx/html/index.html | grep "main\."

# Force rebuild specific service
docker compose build --no-cache frontend

# Remove all build cache
docker builder prune -af
```

### Summary

The issue was caused by Docker's aggressive caching of build layers. When files are renamed or moved (like `LiveMap.bak` → `LiveMap`), Docker may not detect the change if the file contents remain the same. The solution is to:

1. Force a code change (add a comment, touch the file)
2. Remove the old image
3. Build with `--no-cache` flag
4. Verify the new image timestamp

This ensures that all code changes are compiled into the JavaScript bundle and served to the browser.

