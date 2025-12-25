# NoQL Frontend

## Environment variables

### List of variables

- `BACKEND_URL` - base URL of the backend server
- `API_TIMEOUT_MILLIS` - API timeout in milliseconds

### Format

- Env file for development purposes must use prefixes
  ```env
    VITE_BACKEND_URL=http://localhost:8080
    VITE_API_TIMEOUT_MILLIS=40000
    ```
- Other environment files provided in the docker run command don't use the `VITE_` prefix
    ```env
    BACKEND_URL=http://localhost:8080
    API_TIMEOUT_MILLIS=40000
   ```

### How to introduce env

- It is important to update following files:
    - `frontend/NoQL/env.template.js`
    - `frontend/NoQL/src/env.ts`
    - `frontend/NoQL/src/global.d.ts`
    - `frontend/NoQL/.env`

### Logic explained

- Environment variables are dynamically injected to the docker container during start.
- `frontend/NoQL/env.template.js`
    - File act as a template for runtime environment variables.
    - Those placeholders will be replaced by real environment variables when the container starts.
    - It's copied into the Docker image but not filled in yet.
    - During container start, the CMD with envsubst replaces these placeholders with the actual values from the
      container
      environment: `envsubst < /usr/share/nginx/html/env.template.js > /usr/share/nginx/html/env.js`
    - Result `env.js` then contains the actual runtime configuration that frontend will use.
- `frontend/NoQL/src/env.ts`
    - Provides a TypeScript-friendly interface to access environment variables at runtime.
    - Reads the `_env_` object injected by `env.js` (via `window._env_`) in the browser.
    - Fallback logic:
        - If `window._env_` is not defined (e.g., during local development or testing), it uses Vite environment
          variables (`import.meta.env.VITE_*`) instead.
        - Exports a single `ENV` object for the rest of your frontend code to consume consistently.
- `frontend/NoQL/src/global.d.ts`
    - Adds TypeScript typings for window._env_.
    - Ensures that TypeScript recognizes `window._env_` as an optional object with the expected keys.
    - Prevents type errors when you access `window._env_` in `env.ts` or anywhere else in your frontend code.
- `frontend/NoQL/.env`
    - Provide environment variables for development
    - Those variables must start with `VITE_` prefix

## Frontend docker image

- **Build** frontend docker image
    - Version is taken from `package.json` `version` attribute
    ```bash
    npm run dockerBuildFrontend
    ```

- **Build** and **Push** frontend docker image
    - Version is taken from `package.json` `version` attribute
    ```bash
    npm run dockerPushFrontend
    ```

- **Run** frontend docker container
    - Portforward port desired port to `80` in container
    - Define necessary environment variables
    ```bash
    docker run -p 80:80 \
    --env-file ./frontend/NoQL/.env \
    --name noql-frontend \
    janbabak/noql-frontend:0.0.1
    ```
  