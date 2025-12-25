# ---------- Build stage ----------
FROM node:22-alpine AS build

WORKDIR /app

# Install dependencies
COPY package.json package-lock.json ./
RUN npm ci

# Copy source files and build
COPY . .

# Ignore .env files during build
RUN rm -f .env*

RUN npm run build:prod

# ---------- Runtime stage ----------
FROM nginx:alpine AS runtime

# Remove default nginx config
RUN rm /etc/nginx/conf.d/default.conf

# Copy custom nginx config
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy build output from previous stage
COPY --from=build /app/dist /usr/share/nginx/html

# Copy env.template.js to runtime to inject envs
COPY env.template.js /usr/share/nginx/html/env.template.js

EXPOSE 80

# Inject environment variables and start nginx
CMD sh -c "envsubst < /usr/share/nginx/html/env.template.js > /usr/share/nginx/html/env.js && nginx -g 'daemon off;'"