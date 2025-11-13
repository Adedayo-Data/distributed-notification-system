# --- 1. Base Stage ---
# Start with a specific Node.js version for consistency.
FROM node:18-alpine AS base

WORKDIR /usr/src/app

# Copy package.json and lock file
COPY package*.json ./

# --- 2. Dependencies Stage ---
# Install all dependencies, including devDependencies, needed for the build.
FROM base AS dependencies

RUN npm install

# --- 3. Build Stage ---
# Build the TypeScript source code into JavaScript.
FROM dependencies AS build

# Copy the rest of the source code
COPY . .

RUN npm run build

# --- 4. Production Stage ---
# Create the final, lightweight production image.
FROM base AS production

# Copy the built application and production node_modules from previous stages
COPY --from=build /usr/src/app/dist ./dist
COPY --from=dependencies /usr/src/app/node_modules ./node_modules

# Expose the port the application will run on. From your main.ts, the default is 3001.
EXPOSE 3001

# Command to run the application
CMD ["node", "dist/src/main"]
