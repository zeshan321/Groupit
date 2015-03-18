class SearchesController < ApplicationController
  def show
    like_groups = Group.where('name LIKE ?',params[:text]).limit(30)
    render json:like_groups
  end
end
