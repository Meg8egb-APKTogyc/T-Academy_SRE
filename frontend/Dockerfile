FROM nginx:1.29.8-alpine

ENV NGINX_ENVSUBST_VARS='BACKEND_HOST BACKEND_PORT'

COPY nginx.conf.template /etc/nginx/templates/default.conf.template
COPY index.html style.css app.js /usr/share/nginx/html/