class GroupsController < ApplicationController
  include GroupsHelper
  before_filter :authenticate_user, only: [:show,:quick_join,:old_message]
  rescue_from ActiveRecord::RecordNotFound, :with => :page_not_found

  def index
		@groups = Group.all
	end

	def show
		@group = Group.find(params[:id])
    authenticate_access @group
		@messages = @group.messages.last(15)
	end

  def show_qr_code
    @group = Group.find(params[:id])
  end


	def new
		@group = Group.new
	end

  def authorize
    @group = Group.find(params[:id])
    password = params[:password]

    if @group.authenticated?(password)
      user_join @group
      redirect_to @group
    else
      @wrong_password_err = true
      render :join
    end
  end

  def quick_join
    @group = Group.find_by_join_token(params[:join_token])
    if @group.nil?
      redirect_to '/groups/wrong_token'
    else
      user_join @group
      redirect_to @group
    end
  end

  def wrong_token

  end

  def join
    @group = Group.find(params[:id])
    @wrong_password_err = false
  end

	def create
		group_params = params.require(:group)
		@group = Group.new(group_params.permit(:name))
		if params[:public] == 'false'
			@group.public_group = false
			@group.password = group_params[:password]
		end
		if @group.save
      @group.generate_join_token
			redirect_to group_path(@group)
		else
			render 'new'
		end
	end

  def old_message
    @group = Group.find(params[:id])
    authenticate_access @group

    limit = params[:limit].to_i

    if params[:limit].nil?
      limit = 20 #Default limit
    elsif limit > 40 #Max limit
      limit = 40
    elsif limit < 0 #Min limit
      limit = 0
    end

    before = params[:before].to_i
    after = params[:after].to_i
    #Oldest---------------------------------Newest
    #              |       |<~~~~~~~~~~~~>|
    #              |     Limit            |
    #            After                 Before
    if limit == 0
      messages = []

    elsif params[:after].nil? and params[:before].nil?
      messages = @group.messages.pluck(:id,:text,:created_at,:user_id).last(limit+1)

    elsif params[:after] != nil and params[:before] != nil
      if after < before
        messages = @group.messages.where('id > ? AND id < ?',after,before).pluck(:id,:text,:created_at,:user_id).last(limit+1)
      else
        messages = []
      end

    elsif params[:before] != nil
      messages = @group.messages.where('id < ?',before).pluck(:id,:text,:created_at,:user_id).last(limit+1)

    elsif params[:after] != nil
      messages = @group.messages.where('id > ?',after).pluck(:id,:text,:created_at,:user_id).last(limit+1)

    end
    complete = (messages.count <= limit)
    unless complete
      messages = messages[1..-1]
    end
    messages.map! do |message|
      message << User.where('id = ?',message[-1]).pluck(:name).first
    end
    package = [complete,messages]
    render json:package
  end
end
