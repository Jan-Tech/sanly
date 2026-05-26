FROM node:20-alpine
WORKDIR /app

COPY package*.json ./
RUN npm ci --silent

COPY . .

EXPOSE 5173

# --host 0.0.0.0 is required so Vite listens on all interfaces inside Docker.
# The Vite dev proxy then forwards /proxy/* requests to the correct backend
# containers on the internal Docker network (e.g. http://citizen-registry:8080).
# The browser always sees http://localhost:5173 — it never needs to know internal hostnames.
CMD ["npm", "run", "dev", "--", "--host", "0.0.0.0"]
