Rails.application.routes.draw do
  root 'groups#index'

  get '/search' => 'searches#show'

  resources :users, only:[:create, :new, :edit, :update]

  post 'api/users/new' => 'api#create_user'
  post 'api/users/login' => 'api#login_user'
  post 'api' => 'api#init_session'

  resources :groups, only:[:index, :show, :new, :create] do
    resources :messages, only:[:create]
  end

  get 'groups/:id/join' => 'groups#join', as: 'join_group'
  post 'groups/:id/join' => 'groups#authorize'

  get 'join/:join_token' => 'groups#quick_join'
  get 'groups/wrong_token' => 'groups#wrong_token'
  get 'groups/:id/qr' => 'groups#show_qr_code', as: 'group_qr'

  get 'groups/:id/old' => 'groups#old_message'

  # The priority is based upon order of creation: first created -> highest priority.
  # See how all your routes lay out with "rake routes".

  # You can have the root of your site routed with "root"
  # root 'welcome#index'

  # Example of regular route:
  #   get 'products/:id' => 'catalog#view'

  # Example of named route that can be invoked with purchase_url(id: product.id)
  #   get 'products/:id/purchase' => 'catalog#purchase', as: :purchase

  # Example resource route (maps HTTP verbs to controller actions automatically):
  #   resources :products

  # Example resource route with options:
  #   resources :products do
  #     member do
  #       get 'short'
  #       post 'toggle'
  #     end
  #
  #     collection do
  #       get 'sold'
  #     end
  #   end

  # Example resource route with sub-resources:
  #   resources :products do
  #     resources :comments, :sales
  #     resource :seller
  #   end

  # Example resource route with more complex sub-resources:
  #   resources :products do
  #     resources :comments
  #     resources :sales do
  #       get 'recent', on: :collection
  #     end
  #   end

  # Example resource route with concerns:
  #   concern :toggleable do
  #     post 'toggle'
  #   end
  #   resources :posts, concerns: :toggleable
  #   resources :photos, concerns: :toggleable

  # Example resource route within a namespace:
  #   namespace :admin do
  #     # Directs /admin/products/* to Admin::ProductsController
  #     # (app/controllers/admin/products_controller.rb)
  #     resources :products
  #   end
end
